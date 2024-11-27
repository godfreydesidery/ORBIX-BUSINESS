import { Component, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'az-image-uploader',
  standalone: true,
  imports: [],
  templateUrl: './image-uploader.component.html',
  styleUrl: './image-uploader.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class ImageUploaderComponent {
  public image: any;

  fileChange(input: any) {
    const reader = new FileReader();
    if (input.files.length) {
      const file = input.files[0];
      reader.onload = () => {
        this.image = reader.result;
      }
      reader.readAsDataURL(file);
    }
  }

  removeImage(): void {
    this.image = '';
  }
}
