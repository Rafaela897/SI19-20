package communication;

public class Pos {
	public int x;
	public int y;
	
	public Pos(int x,int y) {
		this.x = x;
		this.y = y;
		
	}
	
	@Override
	public int hashCode() {
		return (200*this.x^2 + this.y^3*100)%1000;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(this == o)
			return true;
		if(o instanceof Pos) {
			Pos test = (Pos) o;
			
			return (this.x == test.x && this.y == test.y);
		}
		
		else {
			return false;
		}
	}
}
