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
        order: 0,        
    },
    {
        title: 'Reception',
        routerLink: ' ',
        icon: 'fa-keyboard-o',
        selected: false,
        expanded: false,
        order: 100,
        subMenu: [
            {
                title: 'Vehicle Register',
                routerLink: '/app/parking-management/vehicle-register'
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
        order: 100,
        subMenu: [
            {
                title: 'Invoices',
                url: '#',
                subMenu: [
                    {
                        title: 'Receivable',
                        routerLink: '/app/accounts-and-finance/invoices/receivable-invoice-list'
                    },
                    {
                        title: 'Receivable-Parking',
                        routerLink: '/app/accounts-and-finance/invoices/parking-receivable-invoice-list'
                    },
                    {
                        title: 'Payable',
                        routerLink: '/app/blank'
                    },
                ]
            },
        ]
    },


    {
        title: 'Shop',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        order: 700,
        subMenu: [
            {
                title: 'Sales',
                url: '#',
                subMenu: [
                    {
                        title: 'Sales List',
                        routerLink: '/app/blank'
                    },
                    {
                        title: 'Sales Order',
                        routerLink: '/app/blank'
                    },
                ]
            },
            {
                title: 'Inventory',
                routerLink: '/app/blank'
            },
            {
                title: 'GRN',
                routerLink: '/app/blank'
            },
        ]
    },
    {
        title: 'Procurement',
        routerLink: ' ',
        icon: 'fa-money',
        selected: false,
        expanded: false,
        order: 100,
        subMenu: [
            {
                title: 'LPO',
                routerLink: '/app/blank'
            },
            {
                title: 'BLO',
                routerLink: '/app/blank'
            },
            {
                title: 'GRN',
                routerLink: '/app/blank'
            }
        ]
    },
    {
        title: 'Management',
        routerLink: ' ',
        icon: 'fa-money',
        selected: false,
        expanded: false,
        order: 100,
        subMenu: [
            {
                title: 'Management Board',
                routerLink: '/app/blank'
            },
            {
                title: 'Collection Report',
                routerLink: '/app/blank'
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
    },
    {
        title: 'Admin',
        icon: 'fa-cogs',
        selected: false,
        expanded: false,
        order: 700,
        subMenu: [
            {
                title: 'Identity and Access',
                url: '#',
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
                subMenu: [
                    {
                        title: 'Company',
                        routerLink: '/app/admin-unit/company'
                    },
                    {
                        title: 'Branch',
                        routerLink: '/app/admin-unit/branch'
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
            }
            
        ]
    },
    {
        title: 'System Settings',
        routerLink: ' ',
        icon: 'fa-wrench',
        selected: false,
        expanded: false,
        order: 100,
        subMenu: [
            {
                title: 'System Profile',
                routerLink: '/app/system/system-profile'
            },
        ]
    },
];
