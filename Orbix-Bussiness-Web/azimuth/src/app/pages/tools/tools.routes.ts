import { Routes } from "@angular/router";
import { DragDropComponent } from "./drag-drop/drag-drop.component";
import { ResizableComponent } from "./resizable/resizable.component";
import { ToasterComponent } from "./toaster/toaster.component";

export const routes: Routes = [
    { path: '', redirectTo: 'drag-drop', pathMatch: 'full' },
    { path: 'drag-drop', component: DragDropComponent, data: { breadcrumb: 'Drag and Drop' } },
    { path: 'resizable', component: ResizableComponent, data: { breadcrumb: 'Resizable' } },
    { path: 'toaster', component: ToasterComponent, data: { breadcrumb: 'Toaster' } }
];