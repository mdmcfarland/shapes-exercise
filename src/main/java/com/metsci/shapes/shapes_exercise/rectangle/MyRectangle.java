package com.metsci.shapes.shapes_exercise.rectangle;

import static com.metsci.glimpse.util.ImmutableCollectionUtils.setMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.setPlus;
import static com.metsci.shapes.BoundingBoxUtils.findBoundingBox;
import static com.metsci.shapes.ShapeControlUtils.shapeSelector;
import static com.metsci.shapes.ShapeControlUtils.simpleMouseControl;
import static com.metsci.shapes.ShapeUtils.distance_PX;
import static com.metsci.shapes.ShapeUtils.knobHitRadius_PX;
import static com.metsci.shapes.ShapeUtils.rotatorLeverLength_PX;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_MODIFY;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_RESIZE;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_ROTATE;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_SELECT;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_TRANSLATE;
import static com.metsci.shapes.xy.BoxUtils.boxContains;
import static com.metsci.shapes.xy.BoxUtils.boxResized;
import static com.metsci.shapes.xy.BoxUtils.boxRotated;
import static com.metsci.shapes.xy.BoxUtils.boxRotatorCoords;
import static com.metsci.shapes.xy.BoxUtils.boxTranslated;
import static com.metsci.shapes.xy.BoxUtils.xCorner;
import static com.metsci.shapes.xy.BoxUtils.yCorner;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.metsci.shapes.BoundingBox;
import com.metsci.shapes.DraggablesMouseEvent;
import com.metsci.shapes.Shape;
import com.metsci.shapes.ShapeControl;
import com.metsci.shapes.ShapeFlag;
import com.metsci.shapes.xy.Box;
import com.metsci.shapes.xy.BoxCornerKey;

public class MyRectangle implements Shape
{
    private static final Logger LOGGER = Logger.getLogger(MyRectangle.class.getName());
    
    NumberFormat df = DecimalFormat.getNumberInstance();

    public static final ImmutableSet<? extends ShapeFlag> defaultFlags = ImmutableSet.of( ALLOWS_SELECT, ALLOWS_MODIFY, ALLOWS_TRANSLATE, ALLOWS_RESIZE, ALLOWS_ROTATE );

    private static final double DEFAULT_INTERIOR_PCT = 0.5;
    
    private double interiorX;
    private double interiorY;

    public final Box box;
    public final ImmutableSet<ShapeFlag> flags;

    public MyRectangle( Box box )
    {
        this(box, DEFAULT_INTERIOR_PCT);
    }


    public MyRectangle( Box box, double percentage)
    {
        this( box, defaultFlags, percentage );
    }

    public MyRectangle( Box box, Collection<? extends ShapeFlag> flags )
    {
        this(box, flags, DEFAULT_INTERIOR_PCT);
    }

    public MyRectangle( Box box, Collection<? extends ShapeFlag> flags, double interiorX, double interiorY )
    {
        this.box = box;
        this.flags = ImmutableSet.copyOf( flags );
        setInteriorPoint(interiorX, interiorY);
    }
    

    public MyRectangle( Box box, Collection<? extends ShapeFlag> flags, double percentage )
    {
        this.box = box;
        this.flags = ImmutableSet.copyOf( flags );
        setInteriorPoint(percentage);
    }

    public MyRectangle withBox( Box box)
    {
        return new MyRectangle( box, this.flags );
    }
    
    public MyRectangle withInteriorPoint(double interiorX, double interiorY) {
        LOGGER.info("withInteriorPoint(" + df.format(interiorX) + "," + df.format(interiorY));
        return new MyRectangle( this.box, this.flags, interiorX, interiorY);
    }
    
    public double getInteriorX() {
        return interiorX;
    }

    public double getInteriorY() {
        return interiorY;
    }

    /**
     * Set the interior point to be N percent of the distance
     *    from the top-left corner to the bottom-right corner
     * @param percentage
     */
    public void setInteriorPoint(double percentage) {
        setInteriorPoint(box.xC + percentage * (box.xB-box.xC), box.yC + percentage * (box.yB - box.yC));
    }
    
    public void setInteriorPoint(double x, double y) {
        this.interiorX = x;
        this.interiorY = y;
        LOGGER.warning("interior=" + df.format(x) + "," + df.format(y));
    }
    
    @Override
    public ImmutableSet<? extends ShapeFlag> flags( )
    {
        return this.flags;
    }

    @Override
    public MyRectangle withFlags( Collection<? extends ShapeFlag> flags )
    {
        return new MyRectangle( this.box, flags );
    }

    @Override
    public MyRectangle plusFlags( ShapeFlag... flagsToAdd )
    {
        return this.withFlags( setPlus( this.flags, flagsToAdd ) );
    }

    @Override
    public MyRectangle minusFlags( ShapeFlag... flagsToRemove )
    {
        return this.withFlags( setMinus( this.flags, flagsToRemove ) );
    }

    @Override
    public ShapeControl getControlAt( boolean selected, DraggablesMouseEvent ev )
    {
        // if mouse is near the interior point, return an appropriate control
        double interiorDist = distance_PX( interiorX, interiorY, ev);
        if (distance_PX( interiorX, interiorY, ev) <= knobHitRadius_PX) {
            return this.createInteriorPointMover();
        }
        
        if ( this.flags.contains( ALLOWS_MODIFY ) && selected && this.flags.contains( ALLOWS_RESIZE ) )
        {
            for ( BoxCornerKey cornerKey : BoxCornerKey.values( ) )
            {
                double xCorner = xCorner( this.box, cornerKey );
                double yCorner = yCorner( this.box, cornerKey );
                if ( distance_PX( xCorner, yCorner, ev ) <= knobHitRadius_PX )
                {
                    double xCornerNearMouse = ev.wrapper.x.wrapNear( ev.x, xCorner );
                    double xAxisOffset = xCornerNearMouse - xCorner;
                    double xPixelOffset = ( ev.x - xCornerNearMouse ) * ev.xPpv;

                    double yCornerNearMouse = ev.wrapper.y.wrapNear( ev.y, yCorner );
                    double yAxisOffset = yCornerNearMouse - yCorner;
                    double yPixelOffset = ( ev.y - yCornerNearMouse ) * ev.yPpv;

                    return this.createResizer( cornerKey, xAxisOffset, yAxisOffset, xPixelOffset, yPixelOffset );
                }
            }
        }

        if ( this.flags.contains( ALLOWS_MODIFY ) && selected && this.flags.contains( ALLOWS_ROTATE ) )
        {
            double[] coords = boxRotatorCoords( this.box, ev.xPpv, ev.yPpv, rotatorLeverLength_PX );
            double xR = coords[ 2 ];
            double yR = coords[ 3 ];
            if ( distance_PX( xR, yR, ev ) <= knobHitRadius_PX )
            {
                double xOffset = ev.x - ev.wrapper.x.wrapNear( xR, ev.x );
                double yOffset = ev.y - ev.wrapper.y.wrapNear( yR, ev.y );
                return this.createRotator( xOffset, yOffset );
            }
        }

        if ( this.flags.contains( ALLOWS_MODIFY ) && this.flags.contains( ALLOWS_TRANSLATE ) )
        {
            if ( this.contains( ev ) )
            {
                double xOffset = ev.x - this.box.xCenter;
                double yOffset = ev.y - this.box.yCenter;
                return this.createTranslator( xOffset, yOffset );
            }
        }

        if ( this.flags.contains( ALLOWS_SELECT ) )
        {
            if ( this.contains( ev ) )
            {
                return shapeSelector;
            }
        }

        return null;
    }

    public boolean contains( DraggablesMouseEvent ev )
    {
        BoundingBox bbox = findBoundingBox( this.box );
        for ( double yShift : ev.wrapper.y.getRenderShifts( bbox.yMin, bbox.yMax ) )
        {
            for ( double xShift : ev.wrapper.x.getRenderShifts( bbox.xMin, bbox.xMax ) )
            {
                double x = ev.wrapper.x.wrapValue( ev.x ) - xShift;
                double y = ev.wrapper.y.wrapValue( ev.y ) - yShift;

                if ( boxContains( this.box, x, y ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public ShapeControl createInteriorPointMover() {
        return simpleMouseControl(MyRectangle.class, ( s, ev ) ->
        {
            return s.withInteriorPoint(ev.x, ev.y);
        } );
    }
    
    public ShapeControl createResizer( BoxCornerKey cornerKey, double xAxisOffset, double yAxisOffset, double xPixelOffset, double yPixelOffset )
    {
        return simpleMouseControl( MyRectangle.class, ( s, ev ) ->
        {
            double xCorner = ev.x - ( xPixelOffset / ev.xPpv ) - xAxisOffset;
            double yCorner = ev.y - ( yPixelOffset / ev.yPpv ) - yAxisOffset;
            Box box = boxResized( s.box, cornerKey, xCorner, yCorner );
            return s.withBox( box );
        } );
    }

    public ShapeControl createRotator( double xOffset, double yOffset )
    {
        return simpleMouseControl( MyRectangle.class, ( s, ev ) ->
        {
            double xR = ev.x - xOffset;
            double yR = ev.y - yOffset;
            Box box = boxRotated( s.box, xR, yR );
            return s.withBox( box );
        } );
    }

    public ShapeControl createTranslator( double xOffset, double yOffset )
    {
        return simpleMouseControl( MyRectangle.class, ( s, ev ) ->
        {
            double xCenter = ev.x - xOffset;
            double yCenter = ev.y - yOffset;
            Box box = boxTranslated( s.box, xCenter, yCenter );
            return s.withBox( box );
        } );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 22279;
        int result = 1;
        result = prime * result + Objects.hashCode( this.box );
        result = prime * result + Objects.hashCode( this.flags );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        MyRectangle other = ( MyRectangle ) o;
        return ( Objects.equals( other.box, this.box )
              && Objects.equals( other.flags, this.flags ) );
    }

}
