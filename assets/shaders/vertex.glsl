attribute vec3 a_position;
    attribute vec4 a_color;      // 顶点颜色（含原始Alpha）
    uniform mat4 u_proj;
    uniform mat4 u_model;
    uniform vec4 u_color;
    uniform float u_alpha; // 全局透明度控制
    varying vec4 v_color;

    void main() {
        v_color = a_color * u_color;
        // 全局透明度应用到整个Mesh
        v_color.a *= u_alpha;
        gl_Position = u_proj * u_model * vec4(a_position, 1.0);
    }
