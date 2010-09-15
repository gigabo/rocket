import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import javax.swing.*;

class keydown { 
  public final static int left=0;
  public final static int right=1; 
  public final static int up=2;
  public final static int down=3;
  public final static int space=4;
}

class SpaceThing {
  protected double x, y, a, dx, dy;
  protected SpaceThing next;

  public SpaceThing(){}

  public SpaceThing move (int width, int height, Splode []as){
    if(next != null){ next.move(width, height, as); }
    return this;
  }

  public double getX()	      { return x; }
  public double getY()	      { return y; }
  public Shape getShape()     { return null; }
  public Color getColor()     { return null; }
  public Color getFill()      { return null; }
  public SpaceThing getNext() { return next; }
}
class Halo extends SpaceThing {
  private int radius = 0;
  double rx,ry;
  public Halo(SpaceThing parent, SpaceThing rocket, SpaceThing next){
    x = parent.getX();
    y = parent.getY();
    if(rocket != null && rocket.getClass() == Rocket.class){
      Rocket r = (Rocket)rocket;
      radius = r.getFuel()/4;
      rx = rocket.getX();
      ry = rocket.getY();
    }
    this.next = next;
  }
  public Shape getShape(  ) {
    return (radius > 0)
      ?new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius)
      :null;
  }
  public boolean inRange(){
    if(
      Math.sqrt(Math.pow((x - rx),2)+Math.pow((y - ry),2)) <= radius
    ){ return true; }
    return false;
  }
  public Color getColor(){ return inRange()?Color.green:Color.red; }
}
class HomeBase extends SpaceThing{
  private int radius;
  private double rx, ry;
  public HomeBase(int width, int height, SpaceThing r){
    this.x = width/2;
    this.y = height;
    rx = r.getX();
    ry = r.getY();
    radius = 100;
  }
  public boolean inRange(){
    if(
      Math.sqrt(Math.pow((x - rx),2)+Math.pow((y - ry),2)) <= radius
    ){ return true; }
    return false;
  }
  public Shape getShape(  ) {
    return new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius);
  }
  public Color getColor(){ return Color.blue; }
  public Color getFill(){  return Color.black; }
}
class Splode extends SpaceThing {
  private int ttl,ittl,radius;
  public static Splode next_splode;
  public Splode(double x, double y, int size){
    this.x = x;
    this.y = y;
    ttl = ittl = size;
  }
  public SpaceThing move (int width, int height, Splode []as){
    if(next != null){ next.move(width, height, as); }
    if(ttl > 0){
      ttl--;
      radius = (int)((5*((ittl-ttl)/(double)ittl))*(double)(
	ittl/2-Math.abs(ttl-ittl/2)
      ));
      return this;
    } else {
      return null;
    }
  }
  public Shape getShape(  ) {
    return new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius);
  }
  public boolean intersects(double x, double y){
    if(
      Math.sqrt(Math.pow((x - this.x),2)+Math.pow((y - this.y),2)) <= radius
    ){ return true; }
    return false;
  }
  public Color getColor(){ return Color.red; }
  public Color getFill(){ return new Color(ttl/(float)ittl,0f,0f); }
}
class Baddie extends SpaceThing {
  private int size, delay;
  private double thrust;
  RG parent;
  public Baddie(RG parent, double x, int size) {
    this.parent = parent;
    this.x = Math.random() * x;
    this.y = 0;
    this.size = size;
    thrust = (Math.random()*-2)-.2;
    a = -Math.PI/2 + (Math.random() -.5)*.7;
    delay = (int)(Math.random()*200);
  }

  private void applyThrust(){
    dx = thrust*Math.cos(a);
    dy = thrust*Math.sin(a);
  }

  public SpaceThing move (int width, int height, Splode []as){
    if(delay-- > 0){ return this; }
    applyThrust();
    x += dx;
    y += dy;
    double crap = Math.random();
    int i = 0;
    while(as[i] != null){
      if(as[i].intersects(x, y)){
	return new Splode(x, y, (int)(-.7*size*thrust));
      }
      i++;
    }
    if(x > width  || x < 0){ return new Splode(x, y, size); }
    if(y > height || y < 0){ return new Splode(x, y, size); }
    SpaceThing n = next;
    if(n != null){ n = n.next; }
    next = new Halo(this,parent.rocket,new Trail(this, n));
    next.move(width, height, as);
    return this;
  }

  public Shape getShape(  ) {
    return new Line2D.Double(
      x - 5*Math.cos(a), y - 5*Math.sin(a),
      x + 5*Math.cos(a), y + 5*Math.sin(a)
    );
  }
  public Color getColor(){ return (delay > 0)?Color.black:Color.yellow; }
}
class Rocket extends SpaceThing {
  private int tti, fuel;
  private RG rg;

  public Rocket(double x, double y, RG parent) {
    this.x = x;
    this.y = y;
    rg = parent;
    dy = -5;
    tti = 40;
    fuel = 200;
    a =	Math.PI/2;
  }
  public int getFuel(){
    return fuel;
  }

  private void applyGravity(){
    dy += .2;
  }
  private void applyThrust(){
    if(fuel > 0){
      fuel--;
      dx -= .5*Math.cos(a);
      dy -= .5*Math.sin(a);
    }
  }
  public SpaceThing move (int width, int height, Splode []as){
    applyGravity();
    if(tti > 0){
      tti--;
    } else {
      applyThrust();
    }

    if(rg.key[keydown.left]){ a -= Math.PI/24; }
    if(rg.key[keydown.right]){ a += Math.PI/24; }
    x += dx;
    y += dy;
    if(rg.key[keydown.space] && fuel > 0 && tti <= 0){ 
      return new Splode(x, y, fuel/2); 
    }
    if(x > width  || x < 0){ return new Splode(x, y, fuel/2); }
    if(y > height || y < 0){ return new Splode(x, y, fuel/2); }
    if(tti <= 0 && fuel > 0){
      if(!rg.base.inRange()){
	int i = 0;
	while(as[i] != null){
	  if(as[i].intersects(x, y)){
	    return new Splode(x, y, fuel/2);
	  }
	  i++;
	}
	i = 0;
	while(rg.halos[i] != null){
	  if(rg.halos[i].inRange()){
	    return new Splode(x, y, fuel/2);
	  }
	  i++;
	}
      }
      SpaceThing n = next;
      next = new Trail(this, n);
      next.move(width, height, as);
    }
    return this;
  }

  public Shape getShape(  ) {
    return new Line2D.Double(
      x - 5*Math.cos(a), y - 5*Math.sin(a),
      x + 5*Math.cos(a), y + 5*Math.sin(a)
    );
  }
  public Color getColor(){ return Color.white; }
}
class Trail extends SpaceThing{
  int ittl,ttl;

  public Trail(double x, double y){
    this.x = x;
    this.y = y;
  }
  public Trail(SpaceThing pre, SpaceThing next){
    this.x = pre.x;
    this.y = pre.y;
    this.a = pre.a;
    this.next = next;
    a += (Math.random() -.5)*.5;
    dx = 8*Math.cos(a);
    dy = 8*Math.sin(a);
    ittl = ttl = 10;
  }
  private void applyThrust(){
    dx *= .7;//*dx + 1*Math.cos(a);
    dy *= .7;//*dy + 1*Math.sin(a);
  }
  public SpaceThing move (int width, int height, Splode []as){
    a += (Math.random() -.5)*.4;
    applyThrust();
    x += dx; y += dy;
    if(ttl <= 0){ return null; }
    ttl--;
    if(next != null){ next = next.move(width, height, as); }
    return this;
  }

  public Shape getShape(){
    return new Line2D.Double( x, y, x + 5*Math.cos(a), y + 5*Math.sin(a));
  }
  public Color getColor(){ return new Color(ttl/(float)ittl,0f,0f); }

}



public class RG extends JComponent implements Runnable, KeyListener{
  SpaceThing rocket;
  SpaceThing [] baddie;
  HomeBase base;
  Splode [] all_splodes;
  Halo [] halos;
  Splode [] rsplode;
  int nsplode;
  boolean []key;

  public RG (){
    key = new boolean[256];
    baddie = new SpaceThing[8];
    all_splodes = new Splode[1024];
    halos = new Halo[1024];
    nsplode = 0;
    rsplode = new Splode[16];
    addKeyListener(this);
    setFocusable(true);
    Thread t = new Thread(this);
    t.start(  );
  }

  public void run(  ) {
    try {
      while (true) {
        timeStep(  );
        repaint(  );
        Thread.sleep(1000 / 20);
      }
    }
    catch (InterruptedException ie) {}
  }

  public void paint(Graphics g) {
    Dimension d = getSize(  );
    Graphics2D g2 = (Graphics2D)g;
    g2.setPaint(Color.black);
    g2.fillRect(0,0,d.width,d.height);

    g2.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING, 
      RenderingHints.VALUE_ANTIALIAS_ON
    );

    SpaceThing r;

    for(int i = 0; i < rsplode.length; i++){
      r = rsplode[i];
      if(r != null){
	g2.setPaint(r.getColor());
	Shape s = r.getShape();
	if(s != null){ g2.draw(s); }
	Color f = r.getFill();
	if(f != null){
	  g2.setPaint(f);
	  g2.fill(s);
	}
      }
    }
    for(int i = 0; i < baddie.length; i++){
      r = baddie[i];
      while(r != null){
	g2.setPaint(r.getColor());
	Shape s = r.getShape();
	if(s != null){ g2.draw(s); }
	Color f = r.getFill();
	if(f != null){
	  g2.setPaint(f);
	  g2.fill(s);
	}
	r = r.getNext();
      }
    }
    if(base != null){
      g2.setPaint(base.getColor()); 
      Shape s = base.getShape();
      g2.draw(s);
      g2.setPaint(base.getFill());
      g2.fill(s);
    }
    r = rocket;
    while(r != null){
      g2.setPaint(r.getColor());
      Shape s = r.getShape();
      g2.draw(s);
      Color f = r.getFill();
      if(f != null){
        g2.setPaint(f);
        g2.fill(s);
      }
      r = r.getNext();
    }
  }

  private void getHalos(){
    int n = 0;
    SpaceThing h;
    for(int i = 0; i < baddie.length; i++){
      if(baddie[i].getClass() == Baddie.class){
	h = baddie[i].getNext();
	if(h != null && h.getClass() == Halo.class){
	  halos[n++] = (Halo)h;
	}
      }
    }
    halos[n] = null;
  }

  private void getSplodes(){
    int n = 0;
    for(int i = 0; i < rsplode.length; i++){
      if(rsplode[i] != null){
	all_splodes[n++] = rsplode[i];
      }
    }
    for(int i = 0; i < baddie.length; i++){
      if(baddie[i].getClass() == Splode.class){
	all_splodes[n++] = (Splode)baddie[i];
      }
    }
    all_splodes[n] = null;
  }

  private void timeStep(  ) {
    Dimension d = getSize(  );
    if (d.width == 0 || d.height == 0) return;
    if(rocket == null){ rocket = new Rocket(d.width/2,d.height-10,this); }
    for(int i = 0; i < baddie.length; i++){
      if(baddie[i] == null){ baddie[i] = new Baddie(this, d.width, 100); }
    }
    base = new HomeBase(d.width, d.height, rocket);
    getSplodes();
    getHalos();
    rocket = rocket.move(d.width, d.height, all_splodes);
    if(rocket.getClass() == Splode.class){
      rsplode[nsplode++] = (Splode)rocket;
      nsplode %= rsplode.length;
      rocket = null;
    }
    for(int i = 0; i < rsplode.length; i++){
      if(rsplode[i] != null){
	rsplode[i] = 
	  (Splode)rsplode[i].move(d.width, d.height, all_splodes);
      }
    }
    for(int i = 0; i < baddie.length; i++){
      baddie[i] = baddie[i].move(d.width, d.height, all_splodes);
    }
  }

  public void keyTyped(KeyEvent e){}
  public void downup(KeyEvent e, boolean down){
    int k = e.getKeyCode();
    switch(k){
      case KeyEvent.VK_LEFT   : key[keydown.left]   = down; break;
      case KeyEvent.VK_RIGHT  : key[keydown.right]  = down; break;
      case KeyEvent.VK_DOWN   : key[keydown.down]   = down; break;
      case KeyEvent.VK_UP     : key[keydown.up]     = down; break;
      case KeyEvent.VK_SPACE  : key[keydown.space]  = down; break;
      case KeyEvent.VK_Q      : System.exit(0);
    }
  }
  public void keyPressed(KeyEvent e){ downup(e, true); }
  public void keyReleased(KeyEvent e){ downup(e, false); }


  public static void main(String[] args) {
    JFrame frame = new JFrame("RG");
    frame.getContentPane().add( new RG() );
    frame.setSize(800, 800);
    frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    frame.setVisible(true);
  }
}
