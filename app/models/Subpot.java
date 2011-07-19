package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Subpot extends Model
{
    public int restAmount;
    
    @OneToMany(mappedBy = "subpot", cascade = CascadeType.ALL)
    public List<IndebtAmount> rounds;
    
    public Subpot(int restAmount)
    {
    	this.rounds = new ArrayList<IndebtAmount>();
    }
    
    // TODO(p950nim): Method for adding rounds here?
}
