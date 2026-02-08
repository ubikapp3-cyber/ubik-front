import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, MotelApproval, ApprovalStatistics } from '../../core/services/admin/admin.service';
import { Button01 } from '../../components/button-01/button-01';
import { Button02 } from '../../components/button-02/button-02';

/**
 * Vista del panel de administración
 * Gestiona la aprobación/rechazo de moteles pendientes
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, Button01, Button02],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard implements OnInit {

  // Señales reactivas
  pendingMotels = signal<MotelApproval[]>([]);
  statistics = signal<ApprovalStatistics | null>(null);
  selectedMotel = signal<MotelApproval | null>(null);
  currentFilter = signal<string>('PENDING');
  isLoading = signal(false);
  rejectionReason = signal('');
  showRejectModal = signal(false);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadStatistics();
    this.loadPendingMotels();
  }

  /**
   * Carga las estadísticas de aprobación
   */
  loadStatistics(): void {
    this.adminService.getApprovalStatistics().subscribe({
      next: (stats) => this.statistics.set(stats),
      error: (err) => console.error('Error cargando estadísticas', err)
    });
  }

  /**
   * Carga los moteles pendientes de aprobación
   */
  loadPendingMotels(): void {
    this.isLoading.set(true);
    this.adminService.getPendingMotels().subscribe({
      next: (motels) => {
        this.pendingMotels.set(motels);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error cargando moteles pendientes', err);
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Cambia el filtro de visualización
   */
  changeFilter(status: string): void {
    this.currentFilter.set(status);
    this.isLoading.set(true);

    this.adminService.getMotelsByStatus(status).subscribe({
      next: (motels) => {
        this.pendingMotels.set(motels);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Error filtrando moteles', err);
        this.isLoading.set(false);
      }
    });
  }

  /**
   * Selecciona un motel para ver detalles
   */
  selectMotel(motel: MotelApproval): void {
    this.selectedMotel.set(motel);
  }

  /**
   * Cierra el panel de detalles
   */
  closeDetails(): void {
    this.selectedMotel.set(null);
  }

  /**
   * Aprueba un motel
   */
  approveMotel(motelId: number): void {
    if (!confirm('¿Estás seguro de aprobar este motel?')) {
      return;
    }

    this.adminService.approveMotel(motelId).subscribe({
      next: (response) => {
        console.log('Motel aprobado:', response);
        this.loadPendingMotels();
        this.loadStatistics();
        this.closeDetails();
      },
      error: (err) => console.error('Error aprobando motel', err)
    });
  }

  /**
   * Abre el modal para rechazar un motel
   */
  openRejectModal(motelId: number): void {
    const motel = this.pendingMotels().find(m => m.id === motelId);
    if (motel) {
      this.selectedMotel.set(motel);
      this.showRejectModal.set(true);
    }
  }

  /**
   * Rechaza un motel con una razón
   */
  rejectMotel(): void {
    const motel = this.selectedMotel();
    const reason = this.rejectionReason().trim();

    if (!motel || !reason) {
      alert('Debes proporcionar una razón para el rechazo');
      return;
    }

    this.adminService.rejectMotel(motel.id, reason).subscribe({
      next: (response) => {
        console.log('Motel rechazado:', response);
        this.loadPendingMotels();
        this.loadStatistics();
        this.closeRejectModal();
      },
      error: (err) => console.error('Error rechazando motel', err)
    });
  }

  /**
   * Cierra el modal de rechazo
   */
  closeRejectModal(): void {
    this.showRejectModal.set(false);
    this.rejectionReason.set('');
    this.selectedMotel.set(null);
  }

  /**
   * Pone un motel en revisión
   */
  putUnderReview(motelId: number): void {
    this.adminService.putMotelUnderReview(motelId).subscribe({
      next: (response) => {
        console.log('Motel puesto en revisión:', response);
        this.loadPendingMotels();
        this.loadStatistics();
      },
      error: (err) => console.error('Error poniendo motel en revisión', err)
    });
  }

  /**
   * Obtiene el color para el badge de estado
   */
  getStatusColor(status: string): string {
    const colors: Record<string, string> = {
      'PENDING': 'bg-yellow-500',
      'UNDER_REVIEW': 'bg-blue-500',
      'APPROVED': 'bg-green-500',
      'REJECTED': 'bg-red-500'
    };
    return colors[status] || 'bg-gray-500';
  }

  /**
   * Obtiene el texto traducido del estado
   */
  getStatusText(status: string): string {
    const texts: Record<string, string> = {
      'PENDING': 'Pendiente',
      'UNDER_REVIEW': 'En Revisión',
      'APPROVED': 'Aprobado',
      'REJECTED': 'Rechazado'
    };
    return texts[status] || status;
  }

  /**
   * Verifica si la información legal está completa
   */
  hasCompleteLegalInfo(motel: MotelApproval): boolean {
    return !!(
      motel.rues &&
      motel.rnt &&
      motel.ownerDocumentType &&
      motel.ownerDocumentNumber &&
      motel.ownerFullName
    );
  }
}