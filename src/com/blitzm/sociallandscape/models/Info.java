package com.blitzm.sociallandscape.models;

public class Info {
    //label 
    private String label;
    //value
    private String value;
    
    /**
     * 
     * @param name
     * @param value
     */
    public Info(String name, String value){
        this.label = name;
        this.value = value;
    }
    
    //return the label of each partition
    public String getLabel(){
        return label;
    }
    
    //return the value of each partition
    public String getValue(){
        return value;
    }
}