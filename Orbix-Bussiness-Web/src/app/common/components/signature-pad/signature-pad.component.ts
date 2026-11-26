import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';

/**
 * A box to sign in with the mouse, a touchpad, a finger or a pen.
 *
 * The signature is kept deliberately tiny: it is cropped to the ink, fitted into OUTPUT_WIDTH × OUTPUT_HEIGHT and
 * reduced to pure black and white before it becomes a PNG (about 2 KB). That is enough to show that someone signed,
 * not a faithful copy, which is all the yard needs and keeps the database small.
 *
 * [(signature)] is the PNG as a data URL, or null while nothing is signed.
 * Once signed, the box shows 'caption' (the signer's name) with the date and time under the signature; without a
 * caption it shows 'pendingNote' instead (the driver's name, for one, is only added at registration).
 */
@Component({
  selector: 'az-signature-pad',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="signature-card" [class.signed]="signature">
      <div class="signature-header">
        <span class="signature-title"><i class="fa fa-pencil"></i> {{ label }}<span class="signature-required" *ngIf="required">*</span></span>
        <span class="signature-badge" *ngIf="signature"><i class="fa fa-check-circle"></i> Signed</span>
      </div>
      <div class="signature-box">
        <canvas #canvas width="600" height="200"
          (pointerdown)="start($event)" (pointermove)="move($event)"
          (pointerup)="end()" (pointerleave)="end()" (pointercancel)="end()"></canvas>
        <div class="signature-line"><span>✕</span></div>
        <span class="signature-hint" *ngIf="!signature && !drawing">Sign here</span>
      </div>
      <div class="signature-footer">
        <div class="signature-caption">
          <ng-container *ngIf="signature && signedAt">
            <span *ngIf="caption"><b>{{ caption }}</b> · {{ signedAt | date:'dd-MM-yyyy HH:mm' }}</span>
            <span *ngIf="!caption" class="signature-pending">{{ pendingNote }} · {{ signedAt | date:'dd-MM-yyyy HH:mm' }}</span>
          </ng-container>
        </div>
        <button type="button" class="btn btn-sm btn-outline-danger signature-clear" (click)="clear()" [disabled]="!signature && !drawing">
          <i class="fa fa-eraser"></i> Clear
        </button>
      </div>
    </div>
  `,
  styles: [`
    .signature-card { border: 1px solid #d6dbe1; border-radius: 8px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,.06); margin-bottom: 12px; overflow: hidden; transition: border-color .2s; }
    .signature-card.signed { border-color: #28a745; }
    .signature-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #f5f7fa; border-bottom: 1px solid #e3e7ec; }
    .signature-title { font-weight: 600; color: #2c3e50; }
    .signature-title .fa { color: #1f7a4d; margin-right: 4px; }
    .signature-required { color: #dc3545; margin-left: 2px; }
    .signature-badge { font-size: 12px; color: #28a745; font-weight: 600; }
    .signature-box { position: relative; background: #fcfdfe; }
    .signature-box canvas { position: relative; z-index: 1; display: block; width: 100%; height: auto; aspect-ratio: 3 / 1; touch-action: none; cursor: crosshair; background: transparent; }
    .signature-line { position: absolute; left: 6%; right: 6%; bottom: 22%; border-bottom: 1px dashed #b8c2cc; pointer-events: none; }
    .signature-line span { position: absolute; left: -4px; bottom: 2px; color: #b8c2cc; font-size: 13px; }
    .signature-hint { position: absolute; left: 50%; top: 42%; transform: translate(-50%, -50%); color: #c3cad3; font-style: italic; pointer-events: none; }
    .signature-footer { display: flex; justify-content: space-between; align-items: center; gap: 8px; padding: 6px 12px; border-top: 1px solid #eef1f4; min-height: 40px; }
    .signature-caption { font-size: 12px; color: #4a5560; }
    .signature-pending { color: #8a96a3; font-style: italic; }
    .signature-clear { white-space: nowrap; }
  `]
})
export class SignaturePadComponent implements AfterViewInit, OnChanges {

  static readonly OUTPUT_WIDTH = 200
  static readonly OUTPUT_HEIGHT = 70

  @Input() label: string = 'Signature'
  @Input() required: boolean = true
  /** The signer's name, shown with the date and time once signed */
  @Input() caption: string = ''
  /** Shown instead of a name when the signer's name is not known yet */
  @Input() pendingNote: string = ''

  @Input() signature: string | null = null
  @Output() signatureChange = new EventEmitter<string | null>()

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>

  drawing: boolean = false
  signedAt: Date | null = null
  private inked: boolean = false
  private last: { x: number, y: number } | null = null

  ngAfterViewInit(): void {
    this.reset()
  }

  // The form clearing the signature (after saving, or for a new vehicle) wipes the box too
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['signature'] && !changes['signature'].currentValue && this.canvasRef && this.inked && !this.drawing) {
      this.reset()
    }
  }

  private context(): CanvasRenderingContext2D {
    return this.canvasRef.nativeElement.getContext('2d')!
  }

  // A transparent canvas (the box behind it shows through); the export puts the ink on white
  private reset(): void {
    const canvas = this.canvasRef.nativeElement
    const ctx = this.context()
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = '#000'
    ctx.lineWidth = 4
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    this.inked = false
    this.signedAt = null
  }

  // The pointer's position in the canvas's own pixels, whatever size the box is shown at
  private point(event: PointerEvent): { x: number, y: number } {
    const canvas = this.canvasRef.nativeElement
    const rect = canvas.getBoundingClientRect()
    return {
      x: (event.clientX - rect.left) * canvas.width / rect.width,
      y: (event.clientY - rect.top) * canvas.height / rect.height
    }
  }

  start(event: PointerEvent): void {
    event.preventDefault()
    this.canvasRef.nativeElement.setPointerCapture?.(event.pointerId)
    this.drawing = true
    this.last = this.point(event)
    const ctx = this.context()
    ctx.beginPath()
    ctx.arc(this.last.x, this.last.y, ctx.lineWidth / 2, 0, Math.PI * 2)
    ctx.fillStyle = '#000'
    ctx.fill()
    this.inked = true
  }

  move(event: PointerEvent): void {
    if (!this.drawing || !this.last) return
    event.preventDefault()
    const next = this.point(event)
    const ctx = this.context()
    ctx.beginPath()
    ctx.moveTo(this.last.x, this.last.y)
    ctx.lineTo(next.x, next.y)
    ctx.stroke()
    this.last = next
    this.inked = true
  }

  end(): void {
    if (!this.drawing) return
    this.drawing = false
    this.last = null
    const signature = this.inked ? this.export() : null
    this.signedAt = signature ? new Date() : null
    this.signatureChange.emit(signature)
  }

  clear(): void {
    this.reset()
    this.signatureChange.emit(null)
  }

  /** The ink, cropped, fitted into the output size and turned pure black and white, as a PNG data URL */
  private export(): string | null {
    const canvas = this.canvasRef.nativeElement
    const source = this.context().getImageData(0, 0, canvas.width, canvas.height)

    // The ink's bounding box (the canvas is transparent where nothing is drawn)
    let minX = canvas.width, minY = canvas.height, maxX = -1, maxY = -1
    for (let y = 0; y < canvas.height; y++) {
      for (let x = 0; x < canvas.width; x++) {
        if (source.data[(y * canvas.width + x) * 4 + 3] > 64) {
          if (x < minX) minX = x
          if (x > maxX) maxX = x
          if (y < minY) minY = y
          if (y > maxY) maxY = y
        }
      }
    }
    if (maxX < 0) return null

    const width = maxX - minX + 1
    const height = maxY - minY + 1
    const outWidth = SignaturePadComponent.OUTPUT_WIDTH
    const outHeight = SignaturePadComponent.OUTPUT_HEIGHT
    const scale = Math.min((outWidth - 4) / width, (outHeight - 4) / height, 1)
    const drawWidth = Math.max(1, Math.round(width * scale))
    const drawHeight = Math.max(1, Math.round(height * scale))

    const output = document.createElement('canvas')
    output.width = outWidth
    output.height = outHeight
    const out = output.getContext('2d')!
    out.fillStyle = '#fff'
    out.fillRect(0, 0, outWidth, outHeight)
    out.drawImage(canvas, minX, minY, width, height,
      Math.round((outWidth - drawWidth) / 2), Math.round((outHeight - drawHeight) / 2), drawWidth, drawHeight)

    // Pure black and white: no greys from smoothing, so the PNG compresses well
    const pixels = out.getImageData(0, 0, outWidth, outHeight)
    for (let i = 0; i < pixels.data.length; i += 4) {
      const value = pixels.data[i] < 200 ? 0 : 255
      pixels.data[i] = value
      pixels.data[i + 1] = value
      pixels.data[i + 2] = value
      pixels.data[i + 3] = 255
    }
    out.putImageData(pixels, 0, 0)
    return output.toDataURL('image/png')
  }
}
