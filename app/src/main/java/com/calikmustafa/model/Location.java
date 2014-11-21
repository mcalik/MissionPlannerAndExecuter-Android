package com.calikmustafa.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Mustafa on 21-Nov-14.
 */
public class Location  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int missionID;
    private int soldierID;
    private double latitude;
    private double longitude;
    private String time;
    private String status;

}
