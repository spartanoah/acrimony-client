#ifdef GL_ES
precision mediump float;
#endif

#extension GL_OES_standard_derivatives : enable

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;


vec3 col;


void main( void )
{
    const float PI = 3.1415926535;
    vec2 uv = (gl_FragCoord.xy*2.-resolution.xy)/resolution.y+0.00;

    float w = sin((uv.x + uv.y - time * .5 + sin(1.5 * uv.x + 4.5 * uv.y) * PI * .3) * PI * .6); // fake waviness factor

    col = vec3(0.80,0.80,0.0);
    col = mix(col, vec3(0,0.3,0.8), smoothstep(.01, .025, uv.y+w*.02));
    col = mix(col, vec3(0,0.3,0.8), smoothstep(.65, .75, uv.y+w*.04));
    col += w * .2;

    gl_FragColor = vec4(col, 1.0);
}