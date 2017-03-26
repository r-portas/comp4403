package source;

import java_cup.runtime.ComplexSymbolFactory.Location;
import junit.framework.TestCase;

/**
 * class LineLocationsTest - Junit test of LineLocations class.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public class LineLocationsTest extends TestCase {

    private LineLocations lp;

    public LineLocationsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        lp = new LineLocations();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        lp = null;
    }

    /*
     * Test method for 'pl0.source.LineLocations.LineLocations()'
     */
    public void testLineLocations() {
        assertEquals( 0, lp.getLineNumber( new Location( 0,0 ) ) );
    }

    /*
     * Test method for 'pl0.source.LineLocations.add(Location)'
     */
    public void testAdd() {
        lp.add( 3 );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 1 ) ) );
        assertEquals( 1, lp.getLineNumber( new Location( 1,2 ) ) );
        expectGetLineNumberFail(lp, new Location(1, 4) );
        lp.add( 5 );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 3 ) ) );
        assertEquals( 2, lp.getLineNumber( new Location( 2, 1 ) ) );
        assertEquals( 2, lp.getLineNumber( new Location( 2, 2 ) ) );
        expectGetLineNumberFail(lp, new Location(2, 4) );
        lp.add( 7 );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 0 ) ) );
        assertEquals( 1, lp.getLineNumber( new Location( 1, 3 ) ) );
        assertEquals( 2, lp.getLineNumber( new Location( 2, 2 ) ) );
        assertEquals( 3, lp.getLineNumber( new Location( 3,1 ) ) );
        assertEquals( 3, lp.getLineNumber( new Location( 3, 2 ) ) );
        expectGetLineNumberFail(lp, new Location(3, 5) );
    }

    private void expectGetLineNumberFail(LineLocations lp, Location l) {
        try {
            lp.getLineNumber( l );
            assert false; // Shouldn't be reached
        } catch (AssertionError e) {
            // Desired
        } catch (Exception e) {
            assert false; // Wrong exception
        }
    }

    /*
     * Test method for 'pl0.source.LineLocations.getLineStart(Location)'
     */
    public void testGetLineStart() {
        lp.add( 3 );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 0 ) ) );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 1 ) ) );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 1 ) ) );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 2 ) ) );
        lp.add( 5 );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 1 ) ) );
        assertEquals( 4, (int) lp.getLineStart( new Location( 1, 0 ) ) );
        assertEquals( 4, (int) lp.getLineStart( new Location( 1, 1 ) ) );
        lp.add( 7 );
        assertEquals( 0, (int) lp.getLineStart( new Location( 0, 2 ) ) );
        assertEquals( 4, (int) lp.getLineStart( new Location( 1, 2 ) ) );
        assertEquals( 6, (int) lp.getLineStart( new Location( 2, 1 ) ) );
        assertEquals( 6, (int) lp.getLineStart( new Location( 2, 2 ) ) );
    }

    /*
     * Test method for 'pl0.source.LineLocations.offset(Location)'
     */
    public void testOffset() {
        lp.add(3);
        assertEquals(0, lp.offset(new Location( 1, 0)));
        assertEquals(1, lp.offset(new Location( 1, 1)));
        assertEquals(2, lp.offset(new Location(1, 2)));
        assertEquals(3, lp.offset(new Location(1, 3)));
        lp.add(5);
        assertEquals(3, lp.offset(new Location(1, 3)));
        assertEquals(0, lp.offset(new Location(2, 0)));
        assertEquals(1, lp.offset(new Location(2, 1)));
        lp.add(7);
        assertEquals(3, lp.offset(new Location(1, 3)));
        assertEquals(1, lp.offset(new Location(2, 1)));
        assertEquals(0, lp.offset(new Location(3, 0)));
        assertEquals(1, lp.offset(new Location(3, 1)));
    }

    /*
     * Test method for 'pl0.source.LineLocations.endLast()'
     */
    public void testEndLast() {
        assertEquals( -1, (int) lp.endLast() );
        lp.add( 3 );
        assertEquals( 3, (int) lp.endLast() );
        lp.add( 5 );
        assertEquals( 5, (int) lp.endLast() );
        lp.add( 7 );
        assertEquals( 7, (int) lp.endLast() );
    }

}
