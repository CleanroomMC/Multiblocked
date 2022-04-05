#version 120

attribute vec2 position;

void main(void){
    gl_Position = vec4(position, 0.0, 1.0);
    gl_TexCoord[0].xy = position * 0.5 + 0.5;
}
