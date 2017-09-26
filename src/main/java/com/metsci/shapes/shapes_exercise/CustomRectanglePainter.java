package com.metsci.shapes.shapes_exercise;

import static com.metsci.glimpse.gl.util.GLUtils.disableBlending;
import static com.metsci.glimpse.gl.util.GLUtils.enableStandardBlending;
import static com.metsci.shapes.ShapePainterUtils.shapeThemeDefault;
import static com.metsci.shapes.ShapeUtils.knobDotSize_PX;
import static com.metsci.shapes.ShapeUtils.rotatorLeverLength_PX;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_MODIFY;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_RESIZE;
import static com.metsci.shapes.StandardShapeFlag.ALLOWS_ROTATE;

import javax.media.opengl.GL2ES2;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.shapes.DrawStyle;
import com.metsci.shapes.Shape;
import com.metsci.shapes.ShapeControl;
import com.metsci.shapes.ShapePainter;
import com.metsci.shapes.ShapePainterHelper;
import com.metsci.shapes.ShapeTheme;
import com.metsci.shapes.xy.rectangle.Rectangle;

public class CustomRectanglePainter implements ShapePainter
{

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
        Rectangle s = ( Rectangle ) shape;
        boolean hovered = ( hoveredControl != null );
        boolean selected = ( selectedControl != null );
        
        GL2ES2 gl = context.getGL( ).getGL2ES2( );
        enableStandardBlending( gl );

        DrawStyle shapeStyle = ( hovered ? this.theme.hoveredStyle : this.theme.normalStyle );
        this.helper.drawBox( context, shapeStyle, s.box );

        // find the location 30% of the way from the top-left to the bottom-right corner
        float topLeftX = (float) s.box.xC;
        float bottomRightX = (float) s.box.xB;
        float topLeftY = (float) s.box.yC;
        float bottomRightY = (float) s.box.yB;
        float x = (float) (topLeftX + .3 * (bottomRightX - topLeftX));
        float y = (float)(topLeftY + .3 * (bottomRightY - topLeftY));

        // draw a line from each corner to that point
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xA, s.box.yA, x, y);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xB, s.box.yB, x, y);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xC, s.box.yC, x, y);
        this.helper.lineStrip(context, shapeStyle.outlineThickness_PX, shapeStyle.outlineColor, (float)s.box.xD, s.box.yD, x, y);
        
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
