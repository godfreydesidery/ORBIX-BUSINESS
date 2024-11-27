import { Component, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'az-file-uploader',
  standalone: true,
  imports: [],
  templateUrl: './file-uploader.component.html',
  styleUrl: './file-uploader.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class FileUploaderComponent {
  public file: any;

  fileChange(input: any) {
    const reader = new FileReader();
    if (input.files.length) {
      this.file = input.files[0].name;
    }
  }

  removeFile(): void {
    this.file = '';
  }

}
