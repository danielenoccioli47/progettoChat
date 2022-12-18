package it.fi.itismeucci;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        Client io = new Client();
        io.connetti();
        io.inizia();
    }
}
