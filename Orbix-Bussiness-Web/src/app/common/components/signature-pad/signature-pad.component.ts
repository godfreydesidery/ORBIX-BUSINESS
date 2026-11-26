import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * A box to sign in with the mouse, a finger or a pen.
 *
 * The signature is kept deliberately tiny: it is cropped to the ink, fitted into OUTPUT_WIDTH × OUTPUT_HEIGHT and
 * reduced to pure black and white before it becomes a PNG, usually well under 1 KB. That is enough to show that
 * someone signed, not a faithful copy, which is all the yard needs and keeps the database small.
 *
 * [(signature)] is the PNG as a data URL, or null while nothing is signed.
 * With askName, [(signerName)] is the name typed beside it (for the driver, who is not a system user).
 */
@Component({
  selector: 'az-signature-pad',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="signature-pad">
      <label class="signature-label">{{ label }}<span *ngIf="required">*</span></label>
      <input *ngIf="askName" class="form-control form-control-sm signature-name" type="text"
        [placeholder]="namePlaceholder" [ngModel]="signerName" (ngModelChange)="signerNameChange.emit($event)"
        autocomplete="nope">
      <div class="signature-box" [class.signed]="signature">
        <canvas #canvas width="600" height="200"
          (pointerdown)="start($event)" (pointermove)="move($event)"
          (pointerup)="end($event)" (pointerleave)="end($event)" (pointercancel)="end($event)"></canvas>
        <span class="signature-hint" *ngIf="!signature && !drawing">Sign here</span>
      </div>
      <button type="button" class="btn btn-sm btn-outline-secondary signature-clear" (click)="clear()">
        <i class="fa fa-eraser"></i> Clear
      </button>
    </div>
  `,
  styles: [`
    .signature-pad { margin-bottom: 10px; }
    .signature-label { display: block; font-weight: 600; }
    .signature-name { margin-bottom: 4px; }
    .signature-box { position: relative; border: 1px dashed #999; border-radius: 4px; background: #fff; }
    .signature-box.signed { border-style: solid; border-color: #28a745; }
    .signature-box canvas { display: block; width: 100%; height: auto; aspect-ratio: 3 / 1; touch-action: none; cursor: crosshair; }
    .signature-hint { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); color: #bbb; pointer-events: none; }
    .signature-clear { margin-top: 4px; }
  `]
})
export class SignaturePadComponent implements AfterViewInit, OnChanges {

  static readonly OUTPUT_WIDTH = 200
  static readonly OUTPUT_HEIGHT = 70

  @Input() label: string = 'Signature'
  @Input() required: boolean = true
  @Input() askName: boolean = false
  @Input() namePlaceholder: string = 'Full name'

  @Input() signature: string | null = null
  @Output() signatureChange = new EventEmitter<string | null>()

  @Input() signerName: string = ''
  @Output() signerNameChange = new EventEmitter<string>()

  @ViewChild('canvas') canvasRef!: ElementRef<HTMLCanvasElement>

  drawing: boolean = false
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

  private reset(): void {
    const canvas = this.canvasRef.nativeElement
    const ctx = this.context()
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = '#000'
    ctx.lineWidth = 4
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    this.inked = false
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

  end(event: PointerEvent): void {
    if (!this.drawing) return
    this.drawing = false
    this.last = null
    this.signatureChange.emit(this.inked ? this.export() : null)
  }

  clear(): void {
    this.reset()
    this.signatureChange.emit(null)
  }

  /** The ink, cropped, fitted into the output size and turned pure black and white, as a PNG data URL */
  private export(): string | null {
    const canvas = this.canvasRef.nativeElement
    const source = this.context().getImageData(0, 0, canvas.width, canvas.height)

    // The ink's bounding box
    let minX = canvas.width, minY = canvas.height, maxX = -1, maxY = -1
    for (let y = 0; y < canvas.height; y++) {
      for (let x = 0; x < canvas.width; x++) {
        if (source.data[(y * canvas.width + x) * 4] < 128) {
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

    // Pure black and white: no greys from smoothing, so the PNG compresses to almost nothing
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
