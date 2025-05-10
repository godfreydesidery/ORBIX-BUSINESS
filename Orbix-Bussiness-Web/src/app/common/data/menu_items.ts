import { JwtHelperService } from "@auth0/angular-jwt";
import { DataService } from "@services/custom/data.service";


import { AuthService } from "src/app/auth.service";

export const menuItems = [
    /*{
        title: 'Dashboard',
        routerLink: 'dashboard',
        icon: 'fa-home',
        selected: false,
        expanded: false,
        order: 0
    },
    {
        title: 'Charts',
        routerLink: 'charts',
        icon: 'fa-bar-chart',
        selected: false,
        expanded: false,
        order: 200
    },
    {
        title: 'UI Features',
        routerLink: 'ui',
        icon: 'fa-laptop',
        selected: false,
        expanded: false,
        order: 300,
        subMenu: [
            {
                title: 'Buttons',
                routerLink: 'ui/buttons'
            },
            {
                title: 'Cards',
                routerLink: 'ui/cards'
            },
            {
                title: 'Components',
                routerLink: 'ui/components'
            },
            {
                title: 'Icons',
                routerLink: 'ui/icons'
            },
            {
                title: 'Grid',
                routerLink: 'ui/grid'
            },
            {
                title: 'List Group',
                routerLink: 'ui/list-group'
            },
            {
                title: 'Media Objects',
                routerLink: 'ui/media-objects'
            },
            {
                title: 'Tabs & Accordions',
                routerLink: 'ui/tabs-accordions'
            },
            {
                title: 'Typography',
                routerLink: 'ui/typography'
            }
        ]
    },
    {
        title: 'Tools',
        routerLink: 'tools',
        icon: 'fa-wrench',
        selected: false,
        expanded: false,
        order: 550,
        subMenu: [
            {
                title: 'Drag & Drop',
                routerLink: 'tools/drag-drop'
            },
            {
                title: 'Resizable',
                routerLink: 'tools/resizable'
            },
            {
                title: 'Toastr',
                routerLink: 'tools/toaster'
            }
        ]
    },
    {
        title: 'Mail',
        routerLink: 'mail/mail-list/inbox',
        icon: 'fa-envelope-o',
        selected: false,
        expanded: false,
        order: 330
    },
    {
        title: 'Calendar',
        routerLink: 'calendar',
        icon: 'fa-calendar',
        selected: false,
        expanded: false,
        order: 350
    },
    {
        title: 'Form Elements',
        routerLink: 'form-elements',
        icon: 'fa-pencil-square-o',
        selected: false,
        expanded: false,
        order: 400,
        subMenu: [
            {
                title: 'Form Inputs',
                routerLink: 'form-elements/inputs'
            },
            {
                title: 'Form Layouts',
                routerLink: 'form-elements/layouts'
            },
            {
                title: 'Form Validations',
                routerLink: 'form-elements/validations'
            },
            {
                title: 'Form Wizard',
                routerLink: 'form-elements/wizard'
            }
        ]
    },
    {
        title: 'Tables',
        routerLink: 'tables',
        icon: 'fa-table',
        selected: false,
        expanded: false,
        order: 500,
        subMenu: [
            {
                title: 'Basic Tables',
                routerLink: 'tables/basic-tables'
            },
            {
                title: 'Dynamic Tables',
                routerLink: 'tables/dynamic-tables'
            }
        ]
    },
    {
        title: 'Editors',
        routerLink: 'editors',
        icon: 'fa-pencil',
        selected: false,
        expanded: false,
        order: 550,
        subMenu: [
            {
                title: 'Ckeditor',
                routerLink: 'editors/ckeditor'
            }
        ]
    },
    {
        title: 'Maps',
        routerLink: 'maps',
        icon: 'fa-globe',
        selected: false,
        expanded: false,
        order: 600,
        subMenu: [
            {
                title: 'Vector Maps',
                routerLink: 'maps/vectormaps'
            },
            {
                title: 'Google Maps',
                routerLink: 'maps/googlemaps'
            },
            {
                title: 'Leaflet Maps',
                routerLink: 'maps/leafletmaps'
            }
        ]
    },
    {
        title: 'Pages',
        routerLink: ' ',
        icon: 'fa-file-o',
        selected: false,
        expanded: false,
        order: 650,
        subMenu: [
            {
                title: 'Login',
                routerLink: '/login'
            },
            {
                title: 'Register',
                routerLink: '/register'
            },
            {
                title: 'Blank Page',
                routerLink: 'blank'
            },
            {
                title: 'Error Page',
                routerLink: '/pagenotfound'
            }
        ]
    },
    {
        title: 'Profile',
        routerLink: 'profile',
        icon: 'fa-file-o',
        selected: false,
        expanded: false,
        subMenu: [
            {
                title: 'Projects',
                routerLink: 'profile/projects'
            },
            {
                title: 'User Info',
                routerLink: 'profile/user-info'
            }
        ]
    },
    {
        title: 'Menu Level 1',
        icon: 'fa-ellipsis-h',
        selected: false,
        expanded: false,
        order: 700,
        subMenu: [
            {
                title: 'Menu Level 1.1',
                url: '#',
                disabled: true,
                selected: false,
                expanded: false
            },
            {
                title: 'Menu Level 1.2',
                url: '#',
                subMenu: [{
                    title: 'Menu Level 1.2.1',
                    url: '#',
                    disabled: true,
                    selected: false,
                    expanded: false
                }]
            }
        ]
    },
    {
        title: 'External Link',
        url: 'http://themeseason.com',
        icon: 'fa-external-link',
        selected: false,
        expanded: false,
        order: 800,
        target: '_blank'
    },*/
    {
        title: 'Dashboard',
        routerLink: 'dashboard',
        icon: 'fa-home',
        selected: false,
        expanded: false,
        show : true,
        order: 0,        
    },
    {
        title: 'Reception',
        routerLink: ' ',
        icon: 'fa-keyboard-o',
        selected: false,
        expanded: false,
        show : grant(['RCPTN-ACCESS']),
        order: 100,
        subMenu: [
            {
                title: 'Vehicle Register',
                show : true,
                routerLink: '/app/parking-management/vehicle-register'
            },
            {
                title: 'Parking',
                show : true,
                routerLink: '/app/parking-management/parking'
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Vehicle Registration',
                        routerLink: '/app/management/vehicle-registration-report'
                    } 
                ]
            }

        ]
    },
    /*{
        title: 'Identity and Access',
        routerLink: '',
        icon: 'fa-user-circle-o',
        selected: false,
        expanded: false,
        order: 100,
        subMenu: [
            {
                title: 'User',
                routerLink: '/app/identity-and-access/user'
            },
            {
                title: 'Role',
                routerLink: '/app/identity-and-access/role'
            },
            {
                title: 'Role Access',
                routerLink: '/app/identity-and-access/role-access'
            }
        ]
    },
    {
        title: 'Administration',
        routerLink: ' ',
        icon: 'fa-bank',
        selected: false,
        expanded: false,
        order: 200,
        subMenu: [
            {
                title: 'Company',
                routerLink: '/app/admin-unit/company'
            },
            {
                title: 'Branch',
                routerLink: '/app/admin-unit/branch'
            },
            {
                title: 'Department',
                routerLink: '/app/admin-unit/department'
            },
            {
                title: 'Warehouse',
                routerLink: '/app/admin-unit/warehouse'
            },
            {
                title: 'Shop',
                routerLink: '/app/admin-unit/shop'
            },
            {
                title: 'Shop-Till',
                routerLink: '/app/admin-unit/shop-till'
            },
        ]
    },*/

    {
        title: 'Finance',
        routerLink: ' ',
        icon: 'fa-money',
        selected: false,
        expanded: false,
        show : grant(['FINC-ACCESS']),
        order: 100,
        subMenu: [
            // {
            //     title: 'Invoices',
            //     url: '#',
            //     subMenu: [
            //         // {
            //         //     title: 'Receivable',
            //         //     routerLink: '/app/accounts-and-finance/invoices/receivable-invoice-list'
            //         // },
            //         // {
            //         //     title: 'Receivable-Parking',
            //         //     routerLink: '/app/accounts-and-finance/invoices/parking-receivable-invoice-list'
            //         // },
            //         // {
            //         //     title: 'Payable',
            //         //     routerLink: '/app/blank'
            //         // },
            //     ]
            // },
            {
                title: 'Parking Billing',
                show : grant(['FINCPKNG-ACCESS']),
                routerLink: '/app/accounts-and-finance/parking-billing'
            },
            {
                title: 'Release Vehicles',
                show : grant(['FINCPKNG-ACCESS']),
                routerLink: '/app/accounts-and-finance/release-vehicle-equipment'
            },
            {
                title: 'Storage Billing',
                show : grant(['FINCSTRG-ACCESS']),
                routerLink: '/app/accounts-and-finance/storage-billing'
            },
            {
                title: 'Maintenance Billing',
                show : grant(['FINCMTNC-ACCESS']),
                routerLink: '/app/accounts-and-finance/maintenance-billing'
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Cashier Collections',
                        routerLink: '/app/management/cashier-collections'
                    }, 
                ]
            }
        ]
    },

    {
        title: 'Shop',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        show : grant(['SHOP-ACCESS']),
        order: 700,
        subMenu: [
            // {
            //     title: 'Sales',
            //     url: '#',
            //     subMenu: [
            //         {
            //             title: 'Sales List',
            //             routerLink: '/app/blank'
            //         },
            //         {
            //             title: 'Sales Order',
            //             routerLink: '/app/blank'
            //         },
            //     ]
            // },
            // {
            //     title: 'Inventory',
            //     routerLink: '/app/blank'
            // },
            {
                title: 'Select Shop',
                show : true,
                routerLink: '/app/mechandizing/select-shop'
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Sales Listing Report',
                        routerLink: '/app/mechandizing/sales-listing-report'
                    },
                    {
                        title: 'Fast Moving Products',
                        routerLink: '/app/mechandizing/fast-moving-products-report'
                    },
                ]
            },
            
        ]
    },
    {
        title: 'Warehouse',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        show : grant(['WRHOUSE-ACCESS']),
        order: 700,
        subMenu: [ 
            {
                title: 'Select Warehouse',
                show : true,
                routerLink: '/app/storage-management/select-warehouse'
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Collection Report',
                        routerLink: '/app/storage-management/cash-collections'
                    } 
                ]
            },
        ]
    },
    {
        title: 'Maintenance Mngmt',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        show : grant(['MTNC-ACCESS']),
        //show : true,
        order: 700,
        subMenu: [     
            { 
                title: 'Maintenance',
                url: '#',
                show : true,
                routerLink: '/app/maintenance-management/maintenance'
            },
            { 
                title: 'Verify',
                url: '#',
                show : true,
                routerLink: '/app/maintenance-management/maintenance-verify'
            }, 
              
        ]
    },

    {
        title: 'Technician',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        show : grant(['TCHN-ACCESS']),
        order: 700,
        subMenu: [     
            { 
                title: 'My Jobs',
                url: '#',
                show : true,
                routerLink: '/app/workshop/my-jobs'
            }, 
            { 
                title: 'Verified Jobs',
                url: '#',
                show : true,
                routerLink: '/app/workshop/my-closed-jobs'
            }, 
        ]
    },

    
    {
        title: 'Procurement',
        routerLink: ' ',
        icon: 'fa-money',
        selected: false,
        expanded: false,
        show : grant(['PRCMT-ACCESS']),
        order: 100,
        subMenu: [
            {
                title: 'Suppliers',
                show : true,
                routerLink: '/app/procurement/suppliers'
            },
            // {
            //     title: 'Supplier Product List',
            //     routerLink: '/app/procurement/supplier-product-list'
            // },
            {
                title: 'Supplier Product List',
                show : true,
                routerLink: '/app/procurement/supplier-price-list'
            },
            {
                title: 'Local Purchase Order',
                show : true,
                routerLink: '/app/procurement/lpo'
            },
            {
                title: 'Goods Received Note',
                show : true,
                routerLink: '/app/procurement/grn'
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Local Purchase Order',
                        routerLink: '/app/procurement/reports/lpo-report'
                    },
                    {
                        title: 'Goods Received Note',
                        routerLink: '/app/procurement/reports/grn-report'
                    },               
                ]
            },
            // {
            //     title: 'BLO',
            //     routerLink: '/app/blank'
            // },
            // {
            //     title: 'GRN',
            //     routerLink: '/app/blank'
            // }
        ]
    },
    {
        title: 'Management',
        routerLink: ' ',
        icon: 'fa-money',
        selected: false,
        expanded: false,
        show : grant(['MNGNT-ACCESS']),
        order: 700,
        subMenu: [
            {
                title: 'Management Board',
                show : true,
                routerLink: '/app/management/management-board'
            },
            {
                title: 'Cash Collections',
                show : true,
                routerLink: '/app/management/cash-collections'
            },            
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Vehicle Registration',
                        routerLink: '/app/management/vehicle-registration-report'
                    },                    
                    {
                        title: 'Vehicle Parking',
                        routerLink: '/app/parking-management/reports/parking-report'
                    },
                    {
                        title: 'Sales Listing Report',
                        routerLink: '/app/mechandizing/sales-listing-report'
                    },
                    {
                        title: 'Fast Moving Products',
                        routerLink: '/app/mechandizing/fast-moving-products-report'
                    },                    
                    {
                        title: 'Revenue Report',
                        routerLink: '/app/blank'
                    },
                    {
                        title: 'Sales report',
                        routerLink: '/app/blank'
                    },
                    {
                        title: 'Invoice Report',
                        routerLink: '/app/blank'
                    }
                ]
            }
        ]
    },
    // {
    //     title: 'Inventory',
    //     routerLink: ' ',
    //     icon: 'fa-money',
    //     selected: false,
    //     expanded: false,
    //     show : true,//grant(['MNGNT-ACCESS']),
    //     order: 700,
    //     subMenu: [
    //         {
    //             title: 'Product',
    //             routerLink: '/app/inventory/product'
    //         },
    //         {
    //             title: 'UOM',
    //             routerLink: '/app/inventory/uom'
    //         },
                      
    //         // {
    //         //     title: 'Reports',
    //         //     url: '#',
    //         //     subMenu: [
    //         //         {
    //         //             title: 'Vehicle Registration',
    //         //             routerLink: '/app/management/vehicle-registration-report'
    //         //         },                    
    //         //         {
    //         //             title: 'Vehicle Parking',
    //         //             routerLink: '/app/parking-management/reports/parking-report'
    //         //         },                    
    //         //         {
    //         //             title: 'Revenue Report',
    //         //             routerLink: '/app/blank'
    //         //         },
    //         //         {
    //         //             title: 'Sales report',
    //         //             routerLink: '/app/blank'
    //         //         },
    //         //         {
    //         //             title: 'Invoice Report',
    //         //             routerLink: '/app/blank'
    //         //         }
    //         //     ]
    //         // }
    //     ]
    // },
    {
        title: 'Admin',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        show : grant(['ADMIN-ACCESS']),
        order: 700,
        subMenu: [
            {
                title: 'Identity and Access',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'User',
                        routerLink: '/app/identity-and-access/user'
                    },
                    {
                        title: 'Role',
                        routerLink: '/app/identity-and-access/role'
                    },
                    {
                        title: 'Role Access',
                        routerLink: '/app/identity-and-access/role-access'
                    }
                ]
            },
            {
                title: 'Admin Units',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Company',
                        routerLink: '/app/admin-unit/company'
                    },
                    {
                        title: 'Branch',
                        routerLink: '/app/admin-unit/branch'
                    },
                    {
                        title: 'Shop',
                        routerLink: '/app/admin-unit/shop'
                    },
                   /*  
                   {
                        title: 'Department',
                        routerLink: '/app/admin-unit/department'
                    },
                   {
                        title: 'Warehouse',
                        routerLink: '/app/admin-unit/warehouse'
                    },
                    {
                        title: 'Shop',
                        routerLink: '/app/admin-unit/shop'
                    },
                    {
                        title: 'Shop-Till',
                        routerLink: '/app/admin-unit/shop-till'
                    },*/
                ]
            },
            {
                title: 'Parking Management',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Parking Zone',
                        routerLink: '/app/parking-management/parking-zone'
                    },
                    {
                        title: 'Vehicle Type',
                        routerLink: '/app/parking-management/vehicle-and-equipment-type'
                    },
                    // {
                    //     title: 'Pricing Plan',
                    //     routerLink: '/app/blank'
                    // },
                ]
            },
            {
                title: 'Storage Management',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Warehouse',
                        routerLink: '/app/storage-management/warehouse'
                    },
                    {
                        title: 'Good Types',
                        routerLink: '/app/storage-management/good-type'
                    },
                    // {
                    //     title: 'Vehicle Type',
                    //     routerLink: '/app/parking-management/vehicle-and-equipment-type'
                    // },                  
                ]
            },
            {
                title: 'Maintenance Management',
                url: '#',
                show : true,
                subMenu: [
                    { 
                        title: 'Issue Type',
                        url: '#',
                        show : true,
                        routerLink: '/app/maintenance-management/maintenance-issue-type'
                    }, 
                    { 
                        title: 'Service Specialist',
                        url: '#',
                        show : true,
                        routerLink: '/app/maintenance-management/service-specialist'
                    },             
                ]
            },

              
            
            {
                title: 'Inventory',
                routerLink: ' ',
                icon: 'fa-money',
                selected: false,
                expanded: false,
                show : true,//grant(['MNGNT-ACCESS']),
                order: 700,
                subMenu: [
                    {
                        title: 'Product',
                        show : true,
                        routerLink: '/app/inventory/product'
                    },
                    {
                        title: 'UOM',
                        show : true,
                        routerLink: '/app/inventory/uom'
                    },
                              
                    // {
                    //     title: 'Reports',
                    //     url: '#',
                    //     subMenu: [
                    //         {
                    //             title: 'Vehicle Registration',
                    //             routerLink: '/app/management/vehicle-registration-report'
                    //         },                    
                    //         {
                    //             title: 'Vehicle Parking',
                    //             routerLink: '/app/parking-management/reports/parking-report'
                    //         },                    
                    //         {
                    //             title: 'Revenue Report',
                    //             routerLink: '/app/blank'
                    //         },
                    //         {
                    //             title: 'Sales report',
                    //             routerLink: '/app/blank'
                    //         },
                    //         {
                    //             title: 'Invoice Report',
                    //             routerLink: '/app/blank'
                    //         }
                    //     ]
                    // }
                ]
            },
            {
                title: 'Reports',
                url: '#',
                show : true,
                subMenu: [
                    {
                        title: 'Vehicle Parking Report',
                        routerLink: '/app/parking-management/reports/parking-report'
                    },
                ]
            }
        ]
    },
    // {
    //     title: 'System Settings',
    //     routerLink: ' ',
    //     icon: 'fa-wrench',
    //     selected: false,
    //     expanded: false,
    //     show : true,
    //     order: 100,
    //     subMenu: [
    //         {
    //             title: 'System Profile',
    //             routerLink: '/app/system/system-profile'
    //         },
    //     ]
    // },
];

// export function grant(privilege: string[]): boolean {
//     /** Allow user to perform an action if the user has that privilege */
//     let granted: boolean = false;
//     privilege.forEach(element => {
//         console.log(element)
        

//         var granted : boolean = false
//         let currentUser : {
//         username : string, 
//         access_token : string, 
//         refresh_token : string
//         } = JSON.parse(localStorage.getItem('current-user')!)
//         var privs : {
//             privileges : string[]
//             } = (new JwtHelperService()).decodeToken(currentUser.access_token)! 
//             console.log(privs)

//         for(let i = 0; i < privs.privileges.length; i++){
//             if(privs.privileges[i] === element){
//                 return true
//             }
//         }
//         return granted
//     })
//     return granted
// }




export function grant(privileges: string[]): boolean {
    /** Allow user to perform an action if the user has that privilege */
    
    const currentUser = JSON.parse(localStorage.getItem('current-user')!);
    if (!currentUser || !currentUser.access_token) {
        console.error('No valid user or access token found.');
        return false;
    }

    const decodedToken = new JwtHelperService().decodeToken(currentUser.access_token);
    if (!decodedToken || !decodedToken.privileges) {
        console.error('No privileges found in the token.');
        return false;
    }

    const userPrivileges = decodedToken.privileges as string[];

    // Check if any of the required privileges exist in the user's privileges
    return privileges.some(privilege => userPrivileges.includes(privilege));
}






