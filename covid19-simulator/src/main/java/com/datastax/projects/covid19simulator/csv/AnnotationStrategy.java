package com.datastax.projects.covid19simulator.csv;

import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class AnnotationStrategy extends HeaderColumnNameMappingStrategy
{

    public AnnotationStrategy(Class clazz) {
        super.setType(clazz);
    }

    @Override
    public String[] generateHeader(Object bean) throws CsvRequiredFieldEmptyException
    {
        String[] result=super.generateHeader(bean);
        for(int i=0;i<result.length;i++)
        {
            result[i]=result[i].toLowerCase();
        }
        return result;
    }
}