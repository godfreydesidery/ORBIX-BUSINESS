import { Injectable, Pipe, PipeTransform } from "@angular/core";
import { from } from "rxjs";


@Pipe({
    standalone : true,
    name: 'searchFilter'
})
@Injectable()
export class SearchFilterPipe implements PipeTransform {
    transform(value : any, args? : any) : any {
        if (!value) return null;
        if (!args) return value;

        args = args.toLowerCase()
        debugger
        return value.filter(function(item : any) {
            return JSON.stringify(item)
                .toLowerCase()
                .includes(args);
        });
    }   
}
