package com.example.taxcalc;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public class taxCalculatorImplent implements taxCalculator{

    @WebMethod
    public double taxCalculation(double salary){
        return salary;
    }

    @WebMethod
    public double epfCalculation(double salary) {
        return salary;
    }

    @WebMethod
    public double etfCalculation(double salary) {
        return salary;
    }
}
