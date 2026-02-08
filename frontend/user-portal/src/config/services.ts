export interface Service {
  id: string;
  name: string;
  icon: string;
}

export const SERVICES: Service[] = [
  { id: 'marketplace', name: 'Marketplace', icon: 'ShoppingBag' },
  { id: 'wms', name: 'WMS (Warehouse)', icon: 'Warehouse' },
  { id: 'tms', name: 'TMS (Transport)', icon: 'Truck' },
  { id: 'fms', name: 'FMS (Finance)', icon: 'DollarSign' },
  { id: 'erp', name: 'ERP System', icon: 'Briefcase' },
  { id: 'crm', name: 'CRM (Clients)', icon: 'Users' },
  { id: 'analytics', name: 'Analytics', icon: 'BarChart' },
  { id: 'inventory', name: 'Inventory', icon: 'Box' },
  { id: 'fleet', name: 'Fleet Mgmt', icon: 'Key' },
  { id: 'documents', name: 'Documents', icon: 'FileText' },
  { id: 'support', name: 'Support', icon: 'HelpCircle' }
];