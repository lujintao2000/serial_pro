package com.tuling.domain;

/**
 * @author ljt
 * @date 2020-06-20
 */
public class AboardDepartment extends Department {
    private Country country;

    public AboardDepartment(String name){
        super(name);
    }

    public AboardDepartment(String name,Country country){
        super(name);
        this.country = country;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  AboardDepartment){
            if(super.equals(obj) && ((this.country == null && ((AboardDepartment) obj).country == null) || (this.country != null && this.country.equals(((AboardDepartment) obj).country)))){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int result = 1;
        result *= super.hashCode();
        if(this.country != null){
            result *= this.country.hashCode();
        }
        return result;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
