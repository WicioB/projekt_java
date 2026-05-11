package org.example;

public class Edge
{
    public String label;
    public int uId,vId;
    public double weight;
    public Edge(String label, int uId, int vId, double weight)
    {
        this.label = label;
        this.uId = uId;
        this.vId = vId;
        this.weight = weight;

    }
}
