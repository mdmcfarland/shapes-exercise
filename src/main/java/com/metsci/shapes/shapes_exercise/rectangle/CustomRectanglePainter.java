package com.metsci.shapes.shapes_exercise.rectangle;

import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.shapes.ShapePainterUtils.shapeThemeDefault;
import static com.metsci.shapes.ShapeUtils.knobDotSize_PX;
import static com.metsci.shapes.ShapeUtils.rotatorLeverLength_PX;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_MODIFY;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_RESIZE;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_ROTATE;

import java.util.Random;
import java.util.logging.Logger;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.shapes.DrawStyle;
import com.metsci.shapes.Shape;
import com.metsci.shapes.ShapeControl;
import com.metsci.shapes.ShapePainter;
import com.metsci.shapes.ShapePainterHelper;
import com.metsci.shapes.ShapeTheme;

public class CustomRectanglePainter implements ShapePainter
{
	private static final Logger LOGGER = Logger.getLogger(CustomRectanglePainter.class.getName());

    protected final ShapeTheme theme;
    protected final ShapePainterHelper helper;


    public CustomRectanglePainter( )
    {
        this( shapeThemeDefault );
    }

    public CustomRectanglePainter( ShapeTheme theme )
    {
        this.theme = theme;
        this.helper = new ShapePainterHelper( );
    }

    @Override
    public void paintShape( GlimpseContext context, Object key, Shape shape, ShapeControl hoveredControl, ShapeControl selectedControl )
    {
        MyRectangle s = ( MyRectangle ) shape;
        boolean hovered = ( hoveredControl != null );
        boolean selected = ( selectedControl != null );
        
        GL2ES2 gl = context.getGL( ).getGL2ES2( );
        enableStandardBlending( gl );

        DrawStyle shapeStyle = ( hovered ? this.theme.hoveredStyle : this.theme.normalStyle );
        this.helper.drawBox( context, shapeStyle, s.box );

        // draw a line from each corner to the rectangle's interior location
        float interiorX = (float) s.getInteriorX();
        float interiorY = (float) s.getInteriorY();

        // mark the interior location
        this.helper.drawCircle_PX(context, theme.knobStyle, interiorX, interiorY, 10);

        // ocasionally log where the interiorPt has moved
        if (new Random().nextInt(100) == 0) {
            LOGGER.info("drawing lines to interor point:  " + interiorX + "," + interiorY);
        }
        
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xA, s.box.yA, interiorX, interiorY);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xB, s.box.yB, interiorX, interiorY);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xC, s.box.yC, interiorX, interiorY);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xD, s.box.yD, interiorX, interiorY);
        
        if ( selected )
        {
            this.helper.drawBox( context, this.theme.selectionStyle, s.box );
        }

        if ( s.flags.contains( ALLOWS_MODIFY ) && selected && s.flags.contains( ALLOWS_ROTATE ) )
        {
            this.helper.drawBoxRotator( context, this.theme.leverStyle, this.theme.knobStyle, s.box, rotatorLeverLength_PX, knobDotSize_PX );
        }

        if ( s.flags.contains( ALLOWS_MODIFY ) && selected && s.flags.contains( ALLOWS_RESIZE ) )
        {
            this.helper.drawBoxCorners( context, this.theme.knobStyle, s.box, knobDotSize_PX );
        }

        disableBlending( gl );
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        this.helper.dispose( context );
    }

}
