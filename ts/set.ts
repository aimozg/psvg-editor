export function SetOfKeys(obj:any):{[index:string]:boolean} {
    return Object.keys(obj).reduce((r,e)=>[r[e]=true,r][1],{})
}
export function SetOf(obj:any[]):{[index:string]:boolean} {
    return obj.reduce((r,e)=>[r[''+e]=true,r][1],{})
}
