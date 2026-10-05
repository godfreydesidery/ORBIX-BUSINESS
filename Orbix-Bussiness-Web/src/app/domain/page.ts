// One page of a list, with the number of rows in the whole list
export interface IPage<T> {
    content : T[]
    totalElements : number
}
