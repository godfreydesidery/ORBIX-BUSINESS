import { Injectable } from '@angular/core';
import { menuItems } from '@data/menu_items';

@Injectable()
export class MenuService {

  public getMenuItems(): Array<Object> {
    return menuItems;
  }

}
