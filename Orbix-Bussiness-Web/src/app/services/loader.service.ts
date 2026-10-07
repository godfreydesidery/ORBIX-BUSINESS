import { Injectable, signal } from '@angular/core';

// Requests that finish faster than this never show the loader, so quick calls don't flash it
const SHOW_DELAY_MS = 300
// Once shown, the loader stays at least this long, so it never blinks on and straight off
const MIN_VISIBLE_MS = 500
// Keeps the loader up across back-to-back requests (e.g. save, then reload the list)
const HIDE_DELAY_MS = 150
// After this long the loader tells the user the wait is longer than usual
const SLOW_AFTER_MS = 10000

@Injectable({
  providedIn: 'root'
})
export class LoaderService {

  readonly visible = signal(false)
  readonly slow = signal(false)

  private pending = 0
  private shownAt = 0
  private showTimer: any = null
  private hideTimer: any = null
  private slowTimer: any = null

  start() {
    this.pending++
    clearTimeout(this.hideTimer)
    this.hideTimer = null
    if (!this.visible() && this.showTimer === null) {
      this.showTimer = setTimeout(() => {
        this.showTimer = null
        this.shownAt = Date.now()
        this.visible.set(true)
        this.slowTimer = setTimeout(() => this.slow.set(true), SLOW_AFTER_MS)
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
      const stillToShow = MIN_VISIBLE_MS - (Date.now() - this.shownAt)
      this.hideTimer = setTimeout(() => {
        this.hideTimer = null
        clearTimeout(this.slowTimer)
        this.slow.set(false)
        this.visible.set(false)
      }, Math.max(HIDE_DELAY_MS, stillToShow))
    }
  }
}
