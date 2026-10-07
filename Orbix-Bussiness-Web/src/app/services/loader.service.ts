import { Injectable, signal } from '@angular/core';

// Requests that finish faster than this never show the loader, so quick calls don't flash it
const SHOW_DELAY_MS = 300
// Keeps the loader up across back-to-back requests (e.g. save, then reload the list)
const HIDE_DELAY_MS = 150

@Injectable({
  providedIn: 'root'
})
export class LoaderService {

  readonly visible = signal(false)

  private pending = 0
  private showTimer: any = null
  private hideTimer: any = null

  start() {
    this.pending++
    clearTimeout(this.hideTimer)
    this.hideTimer = null
    if (!this.visible() && this.showTimer === null) {
      this.showTimer = setTimeout(() => {
        this.showTimer = null
        this.visible.set(true)
      }, SHOW_DELAY_MS)
    }
  }

  stop() {
    if (this.pending === 0 || --this.pending > 0) {
      return
    }
    clearTimeout(this.showTimer)
    this.showTimer = null
    if (this.visible()) {
      this.hideTimer = setTimeout(() => {
        this.hideTimer = null
        this.visible.set(false)
      }, HIDE_DELAY_MS)
    }
  }
}
