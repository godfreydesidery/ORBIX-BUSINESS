import { Routes } from "@angular/router";
import { GoogleMapsComponent } from "./google-maps/google-maps.component";
import { LeafletMapsComponent } from "./leaflet-maps/leaflet-maps.component";
import { VectorMapsComponent } from "./vector-maps/vector-maps.component";

export const routes: Routes = [
    { path: '', redirectTo: 'googlemaps', pathMatch: 'full' },
    { path: 'googlemaps', component: GoogleMapsComponent, data: { breadcrumb: 'Google' } },
    { path: 'leafletmaps', component: LeafletMapsComponent, data: { breadcrumb: 'Leaflet' } },
    { path: 'vectormaps', component: VectorMapsComponent, data: { breadcrumb: 'Vector' } }
];