/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.coastalhazards.wps;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jiwalker
 */
public class GenerateTransectsProcessTest {
    
    /**
     * Test of execute method, of class GenerateTransectsProcess.
     */
    @Test
    public void testRotateSegment() throws Exception {
        Coordinate a = new Coordinate(-1, -1);
        Coordinate b = new Coordinate(1, 1);
        LineSegment ls = new LineSegment(a, b);
        double angle = ls.angle();
        double rotated = angle + Angle.PI_OVER_2;
        double rise = 100 * Math.sin(rotated);
        double run = 100 * Math.cos(rotated);
        
        
        System.out.println("x: " + run + " y: " + rise);
    }
    
    @Test
    public void testPreparedWithin() {
        GeometryFactory factory = new GeometryFactory();
        Point a = factory.createPoint(new Coordinate(1,1));
        Point b = factory.createPoint(new Coordinate(2,2));
        LineString line1 = factory.createLineString(new Coordinate[] {new Coordinate(0,0), new Coordinate(3,3)});
        LineString line2 = factory.createLineString(new Coordinate[] {new Coordinate(1.5,1.5), new Coordinate(3,3)});
        Point[] points = new Point[] { a, b };
        MultiPoint createMultiPoint = factory.createMultiPoint(points);
        PreparedGeometry prep = PreparedGeometryFactory.prepare(createMultiPoint);
        assertTrue(prep.within(line1));
        assertFalse(prep.within(line2));
    }
    
}