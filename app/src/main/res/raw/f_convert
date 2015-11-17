#ifdef GL_ES
precision highp float;
#endif

varying vec2 v_texCoord;
uniform sampler2D y_texture;
uniform sampler2D u_texture;
uniform sampler2D v_texture;

void main()
{
    float nx,ny,r,g,b,y,u,v;
    nx=v_texCoord.x;
    ny=v_texCoord.y;
    y=texture2D(y_texture,v_texCoord).r;
    u=texture2D(u_texture,v_texCoord).r;
    v=texture2D(v_texture,v_texCoord).r;

    y=1.1643*(y-0.0625);
    u=u-0.5;
    v=v-0.5;

    r=y+1.5958*v;
    g=y-0.39173*u-0.81290*v;
    b=y+2.017*u;

    gl_FragColor = vec4(r,g,b,1.0);
}