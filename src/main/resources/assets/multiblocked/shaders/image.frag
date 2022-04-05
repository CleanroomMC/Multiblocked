#version 120

uniform sampler2D colourTexture;

void main(void){

    gl_FragColor = texture2D(colourTexture, gl_TexCoord[0].xy).rgba;

}
