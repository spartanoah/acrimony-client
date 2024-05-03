#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
varying vec2 surfacePosition;

#define MAX_ITER 4
void main( void ) {
    vec2 sp = surfacePosition;//vec2(.4, .7);
    vec2 p = sp*5.0 - vec2(10.0);
    vec2 i = p;
    float c = 1.0;

    float inten = 0.01;

    for (int n = 0; n < MAX_ITER; n++)
    {
        float t = time* (11.0 - (3.0 / float(n+1)));
        i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
        c += 1.0/length(vec2(p.x / (sin(i.x+t)/inten),p.y / (cos(i.y+t)/inten)));
    }
    c /= float(MAX_ITER);
    c = 1.5-sqrt(c);
    gl_FragColor = vec4(vec3(c*c*c*c), 999.0) + vec4(0.0, 0.3, 0.5, 1.0);

}