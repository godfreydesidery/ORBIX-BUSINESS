import { ChangeDetectorRef, Component, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'az-multiple-image-uploader',
  standalone: true,
  imports: [],
  templateUrl: './multiple-image-uploader.component.html',
  styleUrl: './multiple-image-uploader.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class MultipleImageUploaderComponent {
  public images: string[] = [];

  constructor(private changeDetectorRef: ChangeDetectorRef) { }

  fileChange(input: any) {
    this.readFiles(input.files);
  }

  readFile(file: any, reader: any, callback: any) {
    reader.onload = () => {
      callback(reader.result);
    }
    reader.readAsDataURL(file);
  }

  readFiles(files: any[], index = 0) {
    let reader = new FileReader();

    if (index in files) {
      this.readFile(files[index], reader, (result: any) => {
        this.images.push(result);
        this.readFiles(files, index + 1);
      });
    } else {
      this.changeDetectorRef.detectChanges();
    }
  }

  removeImage(index: any): void {
    this.images.splice(index, 1);
  }

}
