package validation

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"time"

	"github.com/albelikov/nodeorb/scm-service/internal/worm"
	"github.com/albelikov/nodeorb/scm-service/internal/engine/validation/proto"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

const (
	// Thresholds for manual cost validation
	AuditRequiredThreshold = 0.20 // 20%
	RejectThreshold       = 0.40 // 40%
)

// MarketOracle provides market median price validation
type MarketOracle struct {
	wormStorage worm.Storage
}

// NewMarketOracle creates a new MarketOracle instance
func NewMarketOracle(wormStorage worm.Storage) *MarketOracle {
	return &MarketOracle{
		wormStorage: wormStorage,
	}
}

// ValidateManualCostEntry validates manual cost entry against market median
func (m *MarketOracle) ValidateManualCostEntry(ctx context.Context, req *proto.ManualCostEntryRequest) (*proto.ManualCostEntryResponse, error) {
	// Calculate total input cost
	totalInput := req.MaterialsCost + req.LaborCost

	// Get market median for the order
	medianPrice, err := m.getMarketMedian(ctx, req.OrderId, req.Currency)
	if err != nil {
		return nil, status.Errorf(codes.Internal, "failed to get market median: %v", err)
	}

	// Calculate deviation from median
	deviation := m.calculateDeviation(medianPrice, totalInput)

	// Determine validation status based on thresholds
	var status proto.ValidationStatus
	var auditRequired bool
	var reason string

	if deviation > RejectThreshold {
		// Reject if deviation > 40%
		status = proto.ValidationStatus_VALIDATION_STATUS_REJECTED
		auditRequired = false
		reason = "Significant price deviation > 40%"
	} else if deviation > AuditRequiredThreshold {
		// Require audit if deviation > 20%
		status = proto.ValidationStatus_VALIDATION_STATUS_AUDIT_REQUIRED
		auditRequired = true
		reason = "Price deviation > 20%, requires audit"
	} else {
		// Approve if within acceptable range
		status = proto.ValidationStatus_VALIDATION_STATUS_APPROVED
		auditRequired = false
		reason = "Price within acceptable range"
	}

	// Create validation record for WORM storage
	validationRecord := &worm.ValidationRecord{
		EventID:        req.EventId,
		UserID:         req.UserId,
		OrderID:        req.OrderId,
		MaterialsCost:  req.MaterialsCost,
		LaborCost:      req.LaborCost,
		Currency:       req.Currency,
		Status:         status.String(),
		Deviation:      deviation,
		MedianPrice:    medianPrice,
		SuggestedMedian: medianPrice,
		AuditRequired:  auditRequired,
		Reason:         reason,
		Timestamp:      req.Timestamp.AsTime(),
	}

	// Store validation in WORM storage with hash chaining
	if err := m.wormStorage.StoreValidation(ctx, validationRecord); err != nil {
		return nil, status.Errorf(codes.Internal, "failed to store validation: %v", err)
	}

	return &proto.ManualCostEntryResponse{
		EventId:        req.EventId,
		Status:         status,
		Deviation:      deviation,
		MedianPrice:    medianPrice,
		SuggestedMedian: medianPrice,
		AuditRequired:  auditRequired,
		Reason:         reason,
		ValidatedAt:    time.Now().UTC().Format(time.RFC3339),
	}, nil
}

// SubmitAppeal submits an appeal for a rejected cost entry
func (m *MarketOracle) SubmitAppeal(ctx context.Context, req *proto.AppealRequest) (*proto.AppealResponse, error) {
	// Create appeal record
	appealRecord := &worm.AppealRecord{
		AppealID:    generateAppealID(req.EventId),
		EventID:     req.EventId,
		UserID:      req.UserId,
		Justification: req.Justification,
		EvidenceItems: req.EvidenceItems,
		Status:      proto.AppealStatus_APPEAL_STATUS_SUBMITTED.String(),
		SubmittedAt: time.Now().UTC(),
	}

	// Store appeal in WORM storage
	if err := m.wormStorage.StoreAppeal(ctx, appealRecord); err != nil {
		return nil, status.Errorf(codes.Internal, "failed to store appeal: %v", err)
	}

	return &proto.AppealResponse{
		AppealId:    appealRecord.AppealID,
		Status:      proto.AppealStatus_APPEAL_STATUS_SUBMITTED,
		Message:     "Appeal submitted successfully",
		SubmittedAt: appealRecord.SubmittedAt.Format(time.RFC3339),
	}, nil
}

// getMarketMedian retrieves the market median price for an order
func (m *MarketOracle) getMarketMedian(ctx context.Context, orderID, currency string) (float64, error) {
	// In a real implementation, this would query ClickHouse or another analytics database
	// For now, return a mock median based on order characteristics
	// This should be replaced with actual market data queries
	
	// Mock implementation - in reality this would query historical data
	// grouped by cargo type, distance, region, etc.
	mockMedian := 1000.0 // Mock median price
	
	return mockMedian, nil
}

// calculateDeviation calculates the deviation from the median price
func (m *MarketOracle) calculateDeviation(median, actual float64) float64 {
	if median <= 0 {
		// If no historical data, consider it high risk
		return 1.0
	}
	return (actual - median) / median
}

// generateAppealID generates a unique appeal ID
func generateAppealID(eventID string) string {
	hash := sha256.Sum256([]byte(eventID + time.Now().UTC().String()))
	return hex.EncodeToString(hash[:8])
}

// TrustScoreCalculator calculates dynamic trust scores
type TrustScoreCalculator struct {
	wormStorage worm.Storage
}

// NewTrustScoreCalculator creates a new TrustScoreCalculator instance
func NewTrustScoreCalculator(wormStorage worm.Storage) *TrustScoreCalculator {
	return &TrustScoreCalculator{
		wormStorage: wormStorage,
	}
}

// CalculateTrustScore calculates the current trust score for a user
func (t *TrustScoreCalculator) CalculateTrustScore(ctx context.Context, userID string) (*proto.TrustScoreResponse, error) {
	// Get user history from WORM storage
	history, err := t.wormStorage.GetUserHistory(ctx, userID)
	if err != nil {
		return nil, status.Errorf(codes.Internal, "failed to get user history: %v", err)
	}

	// Calculate component scores
	priceAccuracyScore := t.calculatePriceAccuracyScore(history)
	appealSuccessScore := t.calculateAppealSuccessScore(history)
	biometricsComplianceScore := t.calculateBiometricsComplianceScore(history)
	geographicComplianceScore := t.calculateGeographicComplianceScore(history)
	timeFactorScore := t.calculateTimeFactorScore(history)

	// Weighted average
	trustScore := (
		priceAccuracyScore*0.30 +
		appealSuccessScore*0.25 +
		biometricsComplianceScore*0.20 +
		geographicComplianceScore*0.15 +
		timeFactorScore*0.10
	)

	// Determine trust level
	trustLevel := t.getTrustLevel(trustScore)

	// Create security requirements
	securityRequirements := &proto.SecurityRequirements{
		RequiresBiometrics:   trustScore < 75.0,
		RequiresAppeal:       trustScore < 50.0,
		RequiresManualReview: trustScore < 25.0,
		RestrictedActions:    t.getRestrictedActions(trustLevel),
	}

	return &proto.TrustScoreResponse{
		UserId:             userID,
		TrustScore:         trustScore,
		TrustLevel:         trustLevel,
		LastUpdated:        time.Now().UTC().Format(time.RFC3339),
		SecurityRequirements: securityRequirements,
	}, nil
}

// calculatePriceAccuracyScore calculates score based on price accuracy
func (t *TrustScoreCalculator) calculatePriceAccuracyScore(history *worm.UserHistory) float64 {
	if len(history.Validations) == 0 {
		return 50.0 // Base score
	}

	accurateValidations := 0
	for _, validation := range history.Validations {
		if validation.Status == proto.ValidationStatus_VALIDATION_STATUS_APPROVED.String() {
			accurateValidations++
		}
	}

	accuracyRate := float64(accurateValidations) / float64(len(history.Validations))
	return accuracyRate * 100.0
}

// calculateAppealSuccessScore calculates score based on appeal success rate
func (t *TrustScoreCalculator) calculateAppealSuccessScore(history *worm.UserHistory) float64 {
	if len(history.Appeals) == 0 {
		return 50.0 // Base score
	}

	successfulAppeals := 0
	for _, appeal := range history.Appeals {
		if appeal.Status == proto.AppealStatus_APPEAL_STATUS_APPROVED.String() {
			successfulAppeals++
		}
	}

	successRate := float64(successfulAppeals) / float64(len(history.Appeals))
	return successRate * 100.0
}

// calculateBiometricsComplianceScore calculates score based on biometrics compliance
func (t *TrustScoreCalculator) calculateBiometricsComplianceScore(history *worm.UserHistory) float64 {
	if len(history.AccessChecks) == 0 {
		return 50.0 // Base score
	}

	compliantChecks := 0
	totalBiometricChecks := 0

	for _, check := range history.AccessChecks {
		if check.RequiresBiometrics {
			totalBiometricChecks++
			if check.AccessGranted {
				compliantChecks++
			}
		}
	}

	if totalBiometricChecks == 0 {
		return 50.0
	}

	complianceRate := float64(compliantChecks) / float64(totalBiometricChecks)
	return complianceRate * 100.0
}

// calculateGeographicComplianceScore calculates score based on geographic compliance
func (t *TrustScoreCalculator) calculateGeographicComplianceScore(history *worm.UserHistory) float64 {
	if len(history.GeofenceChecks) == 0 {
		return 50.0 // Base score
	}

	compliantChecks := 0
	for _, check := range history.GeofenceChecks {
		if check.IsInside && check.ViolationReason == "" {
			compliantChecks++
		}
	}

	complianceRate := float64(compliantChecks) / float64(len(history.GeofenceChecks))
	return complianceRate * 100.0
}

// calculateTimeFactorScore calculates score based on user tenure
func (t *TrustScoreCalculator) calculateTimeFactorScore(history *worm.UserHistory) float64 {
	if len(history.Validations) == 0 && len(history.AccessChecks) == 0 && len(history.GeofenceChecks) == 0 {
		return 0.0
	}

	// Find oldest event
	var oldestTime time.Time
	for _, validation := range history.Validations {
		if oldestTime.IsZero() || validation.Timestamp.Before(oldestTime) {
			oldestTime = validation.Timestamp
		}
	}
	for _, check := range history.AccessChecks {
		if oldestTime.IsZero() || check.Timestamp.Before(oldestTime) {
			oldestTime = check.Timestamp
		}
	}
	for _, check := range history.GeofenceChecks {
		if oldestTime.IsZero() || check.Timestamp.Before(oldestTime) {
			oldestTime = check.Timestamp
		}
	}

	if oldestTime.IsZero() {
		return 0.0
	}

	// Calculate days active
	now := time.Now().UTC()
	daysActive := now.Sub(oldestTime).Hours() / 24.0

	// Award 1 point per month, max 50 points
	timeScore := (daysActive / 30.0)
	if timeScore > 50.0 {
		timeScore = 50.0
	}

	return timeScore
}

// getTrustLevel determines the trust level based on score
func (t *TrustScoreCalculator) getTrustLevel(score float64) proto.TrustLevel {
	switch {
	case score < 25.0:
		return proto.TrustLevel_TRUST_LEVEL_CRITICAL
	case score < 50.0:
		return proto.TrustLevel_TRUST_LEVEL_LOW
	case score < 75.0:
		return proto.TrustLevel_TRUST_LEVEL_MEDIUM
	default:
		return proto.TrustLevel_TRUST_LEVEL_HIGH
	}
}

// getRestrictedActions returns restricted actions based on trust level
func (t *TrustScoreCalculator) getRestrictedActions(level proto.TrustLevel) []string {
	switch level {
	case proto.TrustLevel_TRUST_LEVEL_CRITICAL:
		return []string{"place_bid", "view_itar_cargo", "access_sensitive_data", "modify_order"}
	case proto.TrustLevel_TRUST_LEVEL_LOW:
		return []string{"view_itar_cargo", "access_sensitive_data"}
	case proto.TrustLevel_TRUST_LEVEL_MEDIUM:
		return []string{"access_sensitive_data"}
	default:
		return []string{}
	}
}