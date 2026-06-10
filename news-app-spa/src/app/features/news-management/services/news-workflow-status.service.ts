
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface WorkflowStatusConfig {
  status: string;
  label: string;
  labelEs: string;
  color: string;
  icon: string;
  backgroundColor: string;
  textColor: string;
  description: string;
  descriptionEs: string;
  order: number;
  isPublic?: boolean;
  isPublishable?: boolean;
}

@Injectable({ providedIn: 'root' })
export class NewsWorkflowStatusService {
  /**
   * Emits an array of unknown statuses for UI warnings
   */
  private unknownStatusesSubject$ = new BehaviorSubject<string[]>([]);
  public get unknownStatuses$() {
    return this.unknownStatusesSubject$.asObservable();
  }
  private readonly STATUS_STYLING_MAP: Record<string, Partial<WorkflowStatusConfig>> = {
    'DRAFT': {
      color: 'surface',
      backgroundColor: '#e5e7eb',
      textColor: '#4b5563',
      label: 'Draft',
      labelEs: 'Borrador',
      description: 'Article is saved as draft, not yet submitted',
      descriptionEs: 'Artículo guardado como borrador',
      order: 1,
    },
    'PENDING_APPROVAL': {
      color: 'warning',
      backgroundColor: '#fbbf24',
      textColor: '#92400e',
      label: 'Pending Approval',
      labelEs: 'Pendiente de Aprobación',
      description: 'Article is pending approval by an editor',
      descriptionEs: 'Artículo pendiente de aprobación por un editor',
      order: 2,
    },
    'SCHEDULED': {
      color: 'warning',
      backgroundColor: '#f59e0b',
      textColor: '#ffffff',
      label: 'Scheduled',
      labelEs: 'Programado',
      description: 'Article scheduled for publication',
      descriptionEs: 'Artículo programado para publicación',
      order: 3,
    },
    'PUBLISHED': {
      color: 'success',
      backgroundColor: '#22c55e',
      textColor: '#ffffff',
      label: 'Published',
      labelEs: 'Publicado',
      description: 'Article is published and visible to readers',
      descriptionEs: 'Artículo publicado y visible para lectores',
      order: 4,
    },
  };

  constructor() {}

  public getStatusConfig(status: string): WorkflowStatusConfig {
    const styling = this.STATUS_STYLING_MAP[status];
    if (styling) {
      return {
        status,
        label: styling.label || status,
        labelEs: styling.labelEs || status,
        color: styling.color || 'surface',
        icon: '',
        backgroundColor: styling.backgroundColor || '#d1d5db',
        textColor: styling.textColor || '#4b5563',
        description: styling.description || `Status: ${status}`,
        descriptionEs: styling.descriptionEs || `Estado: ${status}`,
        order: styling.order || 999,
        isPublic: styling.isPublic || false,
        isPublishable: styling.isPublishable || false,
      };
    } else {
      // Emit unknown status for UI warning
      const current = this.unknownStatusesSubject$.getValue();
      if (!current.includes(status)) {
        this.unknownStatusesSubject$.next([...current, status]);
      }
      // fallback config for unknown status
      return {
        status,
        label: status,
        labelEs: status,
        color: 'surface',
        icon: '',
        backgroundColor: '#f3f4f6',
        textColor: '#4b5563',
        description: `Unknown status: ${status}`,
        descriptionEs: `Estado desconocido: ${status}`,
        order: 999,
        isPublic: false,
        isPublishable: false,
      };
    }
  }
}