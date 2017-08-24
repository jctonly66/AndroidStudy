package com.study.android.jct.Utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by 10764 on 2017/8/21.
 */
public class CalculatorTest {
    private Calculator mCalculator;

    @Before
    public void setUp() throws Exception{
        mCalculator = new Calculator();
    }


    @Test
    public void sum() throws Exception {
        assertEquals("1+5=6",6d,mCalculator.sum(1d,5d),0);
    }

    @Test
    public void substrac() throws Exception {

    }

    @Test
    public void divide() throws Exception {

    }

    @Test
    public void multiply() throws Exception {

    }

}