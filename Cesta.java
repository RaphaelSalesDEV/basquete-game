/** Cuida do aro e identifica quando uma bola passa por ele. */
public class Cesta {
    // Ajuste estas coordenadas depois de colocar a sua imagem cesta.png.
    public static final double ARO_ESQUERDO = 225;
    public static final double ARO_DIREITO = 375;
    public static final double ALTURA_ARO = 505;

    public boolean marcouPonto(Bola bola, double yAnterior) {
        return bola.getVelocidadeY() > 0
                && yAnterior < ALTURA_ARO
                && bola.getY() >= ALTURA_ARO
                && bola.getX() > ARO_ESQUERDO + Bola.RAIO
                && bola.getX() < ARO_DIREITO - Bola.RAIO;
    }

    public boolean verificarColisoes(Bola bola) {
        boolean bateuEsquerda = bola.quicarNoAro(ARO_ESQUERDO, ALTURA_ARO);
        boolean bateuDireita = bola.quicarNoAro(ARO_DIREITO, ALTURA_ARO);
        return bateuEsquerda || bateuDireita;
    }
}
