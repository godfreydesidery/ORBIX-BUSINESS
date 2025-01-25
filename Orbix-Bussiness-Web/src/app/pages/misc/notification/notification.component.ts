import { Component } from '@angular/core';
import { NotificationService } from 'src/app/notification.service';

@Component({
  selector: 'az-notification',
  standalone: true,
  imports: [],
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.scss'
})
export class NotificationComponent {
  constructor(public notificationService: NotificationService) {}

  // Example to trigger notifications
  triggerSuccess() {
    this.notificationService.addAlert('success', 'Task completed successfully!');
  }

  triggerError() {
    this.notificationService.addAlert('danger', 'An error occurred!');
  }

  get alerts() {
    return this.notificationService.getAlerts();
  }

  closeAlert(index: number) {
    this.notificationService.removeAlert(index);
  }
}
