import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor() { }

  alerts: { type: string; message: string }[] = [];

  // Add a new alert
  addAlert(type: string, message: string) {
    this.alerts.push({ type, message });
  }

  // Get all alerts
  getAlerts() {
    return this.alerts;
  }

  // Clear specific alert
  removeAlert(index: number) {
    this.alerts.splice(index, 1);
  }
}
