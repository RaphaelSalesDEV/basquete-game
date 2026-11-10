import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/** Guarda a sequencia de cestas e salva o melhor recorde. */
public class Pontuacao {
    private int pontos = 0;
    private int recorde = 0;
    private final File arquivo = new File("recorde.txt");

    public Pontuacao() {
        carregar();
    }

    public int getPontos() { return pontos; }
    public int getRecorde() { return recorde; }

    public void acertou() {
        pontos++;
        if (pontos > recorde) {
            recorde = pontos;
            salvar();
        }
    }

    public void errou() {
        pontos = 0;
    }

    private void carregar() {
        try (Scanner leitor = new Scanner(arquivo)) {
            if (leitor.hasNextInt()) {
                recorde = Math.max(0, leitor.nextInt());
            }
        } catch (Exception e) {
            recorde = 0; // Primeira vez jogando: nao existe arquivo ainda.
        }
    }

    private void salvar() {
        try (FileWriter escritor = new FileWriter(arquivo)) {
            escritor.write(Integer.toString(recorde));
        } catch (IOException e) {
            System.out.println("Nao foi possivel salvar o recorde: " + e.getMessage());
        }
    }
}
