package worm

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"time"

	"github.com/albelikov/nodeorb/scm-service/internal/engine/validation/proto"
)

// Storage provides WORM (Write Once, Read Many) storage with hash chaining
type Storage interface {
	StoreValidation(ctx context.Context, record *ValidationRecord) error
	StoreAppeal(ctx context.Context, record *AppealRecord) error
	StoreAccessCheck(ctx context.Context, record *AccessCheckRecord) error
	StoreGeofenceCheck(ctx context.Context, record *GeofenceCheckRecord) error
	GetUserHistory(ctx context.Context, userID string) (*UserHistory, error)
	GetEvidencePackage(ctx context.Context, orderID string) (*EvidencePackage, error)
}

// ValidationRecord represents a validation record
type ValidationRecord struct {
	EventID        string    `json:"event_id"`
	UserID         string    `json:"user_id"`
	OrderID        string    `json:"order_id"`
	MaterialsCost  float64   `json:"materials_cost"`
	LaborCost      float64   `json:"labor_cost"`
	Currency       string    `json:"currency"`
	Status         string    `json:"status"`
	Deviation      float64   `json:"deviation"`
	MedianPrice    float64   `json:"median_price"`
	SuggestedMedian float64   `json:"suggested_median"`
	AuditRequired  bool      `json:"audit_required"`
	Reason         string    `json:"reason"`
	Timestamp      time.Time `json:"timestamp"`
	Hash           string    `json:"hash"`
	PreviousHash   string    `json:"previous_hash"`
}

// AppealRecord represents an appeal record
type AppealRecord struct {
	AppealID      string            `json:"appeal_id"`
	EventID       string            `json:"event_id"`
	UserID        string            `json:"user_id"`
	Justification string            `json:"justification"`
	EvidenceItems []*proto.EvidenceItem `json:"evidence_items"`
	Status        string            `json:"status"`
	SubmittedAt   time.Time         `json:"submitted_at"`
	Hash          string            `json:"hash"`
	PreviousHash  string            `json:"previous_hash"`
}

// AccessCheckRecord represents an access check record
type AccessCheckRecord struct {
	ID               string    `json:"id"`
	UserID           string    `json:"user_id"`
	OrderID          string    `json:"order_id"`
	AccessGranted    bool      `json:"access_granted"`
	Timestamp        time.Time `json:"timestamp"`
	Reason           string    `json:"reason"`
	RequiresBiometrics bool    `json:"requires_biometrics"`
	Hash             string    `json:"hash"`
	PreviousHash     string    `json:"previous_hash"`
}

// GeofenceCheckRecord represents a geofence check record
type GeofenceCheckRecord struct {
	ID               string    `json:"id"`
	UserID           string    `json:"user_id"`
	OrderID          string    `json:"order_id"`
	IsInside         bool      `json:"is_inside"`
	Latitude         float64   `json:"latitude"`
	Longitude        float64   `json:"longitude"`
	Timestamp        time.Time `json:"timestamp"`
	ViolationReason  string    `json:"violation_reason"`
	Hash             string    `json:"hash"`
	PreviousHash     string    `json:"previous_hash"`
}

// UserHistory represents a user's complete history
type UserHistory struct {
	UserID        string               `json:"user_id"`
	Validations   []*ValidationRecord  `json:"validations"`
	Appeals       []*AppealRecord      `json:"appeals"`
	AccessChecks  []*AccessCheckRecord `json:"access_checks"`
	GeofenceChecks []*GeofenceCheckRecord `json:"geofence_checks"`
}

// EvidencePackage represents a complete evidence package
type EvidencePackage struct {
	OrderID       string               `json:"order_id"`
	GeneratedAt   time.Time            `json:"generated_at"`
	Events        []*proto.ComplianceEvent `json:"events"`
	Validations   []*proto.ManualEntryValidation `json:"validations"`
	AccessChecks  []*proto.AccessCheck `json:"access_checks"`
	GeofenceChecks []*proto.GeofenceCheck `json:"geofence_checks"`
	HashChain     []*proto.HashChainNode `json:"hash_chain"`
	RootHash      string               `json:"root_hash"`
	MerkleRoot    string               `json:"merkle_root"`
	Signature     string               `json:"signature"`
}

// InMemoryStorage implements Storage interface with in-memory storage
type InMemoryStorage struct {
	validations   map[string]*ValidationRecord
	appeals       map[string]*AppealRecord
	accessChecks  map[string]*AccessCheckRecord
	geofenceChecks map[string]*GeofenceCheckRecord
	userHistories map[string]*UserHistory
}

// NewInMemoryStorage creates a new in-memory storage
func NewInMemoryStorage() *InMemoryStorage {
	return &InMemoryStorage{
		validations:   make(map[string]*ValidationRecord),
		appeals:       make(map[string]*AppealRecord),
		accessChecks:  make(map[string]*AccessCheckRecord),
		geofenceChecks: make(map[string]*GeofenceCheckRecord),
		userHistories: make(map[string]*UserHistory),
	}
}

// StoreValidation stores a validation record with hash chaining
func (s *InMemoryStorage) StoreValidation(ctx context.Context, record *ValidationRecord) error {
	// Calculate hash
	record.Hash = s.calculateHash(record)
	
	// Get previous hash for chaining
	record.PreviousHash = s.getPreviousHash(record.UserID)
	
	// Store record
	s.validations[record.EventID] = record
	
	// Update user history
	s.updateUserHistory(record.UserID, record, nil, nil, nil)
	
	return nil
}

// StoreAppeal stores an appeal record with hash chaining
func (s *InMemoryStorage) StoreAppeal(ctx context.Context, record *AppealRecord) error {
	// Calculate hash
	record.Hash = s.calculateAppealHash(record)
	
	// Get previous hash for chaining
	record.PreviousHash = s.getPreviousHash(record.UserID)
	
	// Store record
	s.appeals[record.AppealID] = record
	
	// Update user history
	s.updateUserHistory(record.UserID, nil, record, nil, nil)
	
	return nil
}

// StoreAccessCheck stores an access check record with hash chaining
func (s *InMemoryStorage) StoreAccessCheck(ctx context.Context, record *AccessCheckRecord) error {
	// Calculate hash
	record.Hash = s.calculateAccessCheckHash(record)
	
	// Get previous hash for chaining
	record.PreviousHash = s.getPreviousHash(record.UserID)
	
	// Store record
	s.accessChecks[record.ID] = record
	
	// Update user history
	s.updateUserHistory(record.UserID, nil, nil, record, nil)
	
	return nil
}

// StoreGeofenceCheck stores a geofence check record with hash chaining
func (s *InMemoryStorage) StoreGeofenceCheck(ctx context.Context, record *GeofenceCheckRecord) error {
	// Calculate hash
	record.Hash = s.calculateGeofenceCheckHash(record)
	
	// Get previous hash for chaining
	record.PreviousHash = s.getPreviousHash(record.UserID)
	
	// Store record
	s.geofenceChecks[record.ID] = record
	
	// Update user history
	s.updateUserHistory(record.UserID, nil, nil, nil, record)
	
	return nil
}

// GetUserHistory retrieves a user's complete history
func (s *InMemoryStorage) GetUserHistory(ctx context.Context, userID string) (*UserHistory, error) {
	history, exists := s.userHistories[userID]
	if !exists {
		return &UserHistory{
			UserID:        userID,
			Validations:   make([]*ValidationRecord, 0),
			Appeals:       make([]*AppealRecord, 0),
			AccessChecks:  make([]*AccessCheckRecord, 0),
			GeofenceChecks: make([]*GeofenceCheckRecord, 0),
		}, nil
	}
	return history, nil
}

// GetEvidencePackage retrieves a complete evidence package for an order
func (s *InMemoryStorage) GetEvidencePackage(ctx context.Context, orderID string) (*EvidencePackage, error) {
	// Collect all relevant records for the order
	var validations []*proto.ManualEntryValidation
	var accessChecks []*proto.AccessCheck
	var geofenceChecks []*proto.GeofenceCheck
	var events []*proto.ComplianceEvent
	
	// Collect validations
	for _, v := range s.validations {
		if v.OrderID == orderID {
			validations = append(validations, &proto.ManualEntryValidation{
				Id:              v.EventID,
				UserId:          v.UserID,
				OrderId:         v.OrderID,
				MaterialsCost:   v.MaterialsCost,
				LaborCost:       v.LaborCost,
				Currency:        v.Currency,
				RiskVerdict:     proto.ValidationStatus(proto.ValidationStatus_value[v.Status]),
				AiConfidenceScore: 1.0, // Mock value
				RequiresAppeal:  v.AuditRequired,
				CreatedAt:       v.Timestamp.Format(time.RFC3339),
			})
		}
	}
	
	// Collect access checks
	for _, a := range s.accessChecks {
		if a.OrderID == orderID {
			accessChecks = append(accessChecks, &proto.AccessCheck{
				Id:            a.ID,
				UserId:        a.UserID,
				OrderId:       a.OrderID,
				AccessGranted: a.AccessGranted,
				Timestamp:     a.Timestamp.Format(time.RFC3339),
				Reason:        a.Reason,
			})
		}
	}
	
	// Collect geofence checks
	for _, g := range s.geofenceChecks {
		if g.OrderID == orderID {
			geofenceChecks = append(geofenceChecks, &proto.GeofenceCheck{
				Id:              g.ID,
				UserId:          g.UserID,
				OrderId:         g.OrderID,
				IsInside:        g.IsInside,
				Latitude:        g.Latitude,
				Longitude:       g.Longitude,
				Timestamp:       g.Timestamp.Format(time.RFC3339),
				ViolationReason: g.ViolationReason,
			})
		}
	}
	
	// Create compliance events
	for _, v := range validations {
		events = append(events, &proto.ComplianceEvent{
			EventId:   v.Id,
			EventType: "MANUAL_COST_VALIDATION",
			Timestamp: v.CreatedAt,
			UserId:    v.UserId,
			Details: map[string]string{
				"status": v.RiskVerdict.String(),
				"order_id": v.OrderId,
			},
		})
	}
	
	// Create hash chain
	hashChain := s.createHashChain(orderID)
	
	// Calculate Merkle root
	merkleRoot := s.calculateMerkleRoot(hashChain)
	
	// Create evidence package
	evidencePackage := &EvidencePackage{
		OrderID:       orderID,
		GeneratedAt:   time.Now().UTC(),
		Events:        events,
		Validations:   validations,
		AccessChecks:  accessChecks,
		GeofenceChecks: geofenceChecks,
		HashChain:     hashChain,
		RootHash:      hashChain[len(hashChain)-1].Hash,
		MerkleRoot:    merkleRoot,
		Signature:     s.signEvidencePackage(orderID, merkleRoot),
	}
	
	return evidencePackage, nil
}

// calculateHash calculates the hash for a validation record
func (s *InMemoryStorage) calculateHash(record *ValidationRecord) string {
	data := fmt.Sprintf("%s%s%s%f%f%s%s%f%s%s",
		record.EventID,
		record.UserID,
		record.OrderID,
		record.MaterialsCost,
		record.LaborCost,
		record.Currency,
		record.Status,
		record.Deviation,
		record.Reason,
		record.Timestamp.Format(time.RFC3339),
	)
	
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

// calculateAppealHash calculates the hash for an appeal record
func (s *InMemoryStorage) calculateAppealHash(record *AppealRecord) string {
	evidenceJSON, _ := json.Marshal(record.EvidenceItems)
	data := fmt.Sprintf("%s%s%s%s%s%s",
		record.AppealID,
		record.EventID,
		record.UserID,
		record.Justification,
		string(evidenceJSON),
		record.SubmittedAt.Format(time.RFC3339),
	)
	
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

// calculateAccessCheckHash calculates the hash for an access check record
func (s *InMemoryStorage) calculateAccessCheckHash(record *AccessCheckRecord) string {
	data := fmt.Sprintf("%s%s%s%t%s%t%s",
		record.ID,
		record.UserID,
		record.OrderID,
		record.AccessGranted,
		record.Reason,
		record.RequiresBiometrics,
		record.Timestamp.Format(time.RFC3339),
	)
	
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

// calculateGeofenceCheckHash calculates the hash for a geofence check record
func (s *InMemoryStorage) calculateGeofenceCheckHash(record *GeofenceCheckRecord) string {
	data := fmt.Sprintf("%s%s%s%t%f%f%s%s",
		record.ID,
		record.UserID,
		record.OrderID,
		record.IsInside,
		record.Latitude,
		record.Longitude,
		record.ViolationReason,
		record.Timestamp.Format(time.RFC3339),
	)
	
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}

// getPreviousHash gets the previous hash for a user
func (s *InMemoryStorage) getPreviousHash(userID string) string {
	// In a real implementation, this would query the database for the latest hash
	// For now, return empty string for first entry
	return ""
}

// updateUserHistory updates the user's history with new records
func (s *InMemoryStorage) updateUserHistory(userID string, validation *ValidationRecord, appeal *AppealRecord, accessCheck *AccessCheckRecord, geofenceCheck *GeofenceCheckRecord) {
	history, exists := s.userHistories[userID]
	if !exists {
		history = &UserHistory{
			UserID:        userID,
			Validations:   make([]*ValidationRecord, 0),
			Appeals:       make([]*AppealRecord, 0),
			AccessChecks:  make([]*AccessCheckRecord, 0),
			GeofenceChecks: make([]*GeofenceCheckRecord, 0),
		}
		s.userHistories[userID] = history
	}
	
	if validation != nil {
		history.Validations = append(history.Validations, validation)
	}
	if appeal != nil {
		history.Appeals = append(history.Appeals, appeal)
	}
	if accessCheck != nil {
		history.AccessChecks = append(history.AccessChecks, accessCheck)
	}
	if geofenceCheck != nil {
		history.GeofenceChecks = append(history.GeofenceChecks, geofenceCheck)
	}
}

// createHashChain creates a hash chain for an order
func (s *InMemoryStorage) createHashChain(orderID string) []*proto.HashChainNode {
	var nodes []*proto.HashChainNode
	var previousHash string
	
	// Collect all records for the order and sort by timestamp
	var records []struct {
		id      string
		typeStr string
		timestamp time.Time
		hash    string
	}
	
	for _, v := range s.validations {
		if v.OrderID == orderID {
			records = append(records, struct {
				id      string
				typeStr string
				timestamp time.Time
				hash    string
			}{v.EventID, "VALIDATION", v.Timestamp, v.Hash})
		}
	}
	
	for _, a := range s.accessChecks {
		if a.OrderID == orderID {
			records = append(records, struct {
				id      string
				typeStr string
				timestamp time.Time
				hash    string
			}{a.ID, "ACCESS_CHECK", a.Timestamp, a.Hash})
		}
	}
	
	for _, g := range s.geofenceChecks {
		if g.OrderID == orderID {
			records = append(records, struct {
				id      string
				typeStr string
				timestamp time.Time
				hash    string
			}{g.ID, "GEOFENCE_CHECK", g.Timestamp, g.Hash})
		}
	}
	
	// Sort by timestamp
	for i := 0; i < len(records)-1; i++ {
		for j := i + 1; j < len(records); j++ {
			if records[i].timestamp.After(records[j].timestamp) {
				records[i], records[j] = records[j], records[i]
			}
		}
	}
	
	// Create hash chain nodes
	for _, record := range records {
		node := &proto.HashChainNode{
			Id:           record.id,
			Type:         record.typeStr,
			Hash:         record.hash,
			PreviousHash: previousHash,
			Timestamp:    record.timestamp.Format(time.RFC3339),
		}
		nodes = append(nodes, node)
		previousHash = record.hash
	}
	
	return nodes
}

// calculateMerkleRoot calculates the Merkle root for a hash chain
func (s *InMemoryStorage) calculateMerkleRoot(hashChain []*proto.HashChainNode) string {
	if len(hashChain) == 0 {
		return ""
	}
	
	// For simplicity, we'll use a simple hash of all hashes
	// In a real implementation, this would be a proper Merkle tree
	var allHashes string
	for _, node := range hashChain {
		allHashes += node.Hash
	}
	
	hash := sha256.Sum256([]byte(allHashes))
	return hex.EncodeToString(hash[:])
}

// signEvidencePackage signs an evidence package
func (s *InMemoryStorage) signEvidencePackage(orderID, merkleRoot string) string {
	// In a real implementation, this would use a private key to sign
	// For now, return a mock signature
	data := fmt.Sprintf("%s%s", orderID, merkleRoot)
	hash := sha256.Sum256([]byte(data))
	return hex.EncodeToString(hash[:])
}