
package com.beca.ms1_users.adapters.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.beca.ms1_users.application.port.in.ImportUserRowCommand;

class UserCsvParserTest {

    private final UserCsvParser parser = new UserCsvParser();

    @Test
    void deveInterpretarArquivoCsvSeparadoPorVirgula() throws Exception {

        String csv = """
                nome,email,senha
                Maria,maria@email.com,123456
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, usuarios.size());
        assertEquals("Maria", usuarios.get(0).nome());
        assertEquals("maria@email.com", usuarios.get(0).email());
        assertEquals("123456", usuarios.get(0).senha());
    }

    @Test
    void deveInterpretarArquivoCsvComPontoEVirgula() throws Exception {

        String csv = """
                nome;email;senha
                Maria;maria@email.com;123456
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, usuarios.size());
        assertEquals("Maria", usuarios.get(0).nome());
        assertEquals("maria@email.com", usuarios.get(0).email());
    }

    @Test
    void deveInterpretarCabecalhosSemDiferenciarMaiusculas() throws Exception {

        String csv = """
                NOME,EMAIL,SENHA
                Maria,maria@email.com,123456
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, usuarios.size());
        assertEquals("Maria", usuarios.get(0).nome());
    }

    @Test
    void deveInterpretarArquivoComBomUtf8() throws Exception {

        String csv = "\uFEFFnome,email,senha\n"
                + "Maria,maria@email.com,123456";

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(1, usuarios.size());
        assertEquals("Maria", usuarios.get(0).nome());
        assertEquals("maria@email.com", usuarios.get(0).email());
    }

    @Test
    void deveLancarExcecaoQuandoArquivoEstiverVazio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new byte[0])
        );
    }

    @Test
    void deveLancarExcecaoQuandoCabecalhosForemInvalidos() {

        String csv = """
                nome,telefone,senha
                Maria,999999,123456
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(csv.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void deveManterNumeracaoFisicaDasLinhasAposLinhaEmBranco()
            throws Exception {

        String csv = """
                nome,email,senha
                Maria,maria@email.com,123456

                Joao,joao@email.com,654321
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, usuarios.size());
        assertEquals(2, usuarios.get(0).numeroLinha());
        assertEquals(4, usuarios.get(1).numeroLinha());
    }

    @Test
    void deveIdentificarRegistroComQuantidadeIncorretaDeColunas()
            throws Exception {

        String csv = """
                nome,email,senha
                Maria,maria@email.com,123456
                Joao,joao@email.com
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, usuarios.size());
        assertEquals("Maria", usuarios.get(0).nome());

        assertEquals(3, usuarios.get(1).numeroLinha());
        assertNull(usuarios.get(1).nome());
        assertNull(usuarios.get(1).email());
        assertNull(usuarios.get(1).senha());
    }

    @Test
    void deveRejeitarLinhaComAspasNaoFechadas() throws Exception {

        String csv = """
                nome,email,senha
                "Maria,maria@email.com,123456
                Ana,ana@email.com,654321
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(2, usuarios.size());

        assertEquals(2, usuarios.get(0).numeroLinha());
        assertNull(usuarios.get(0).nome());

        assertEquals(3, usuarios.get(1).numeroLinha());
        assertEquals("Ana", usuarios.get(1).nome());
    }

    @Test
    void deveContinuarProcessamentoAposAspasInvalidas() throws Exception {

        String csv = """
                nome,email,senha
                Maria,maria@email.com,123456
                "Joao,joao@email.com,123456
                Pedro,pedro@email.com,654321
                """;

        List<ImportUserRowCommand> usuarios =
                parser.parse(csv.getBytes(StandardCharsets.UTF_8));

        assertEquals(3, usuarios.size());

        assertEquals("Maria", usuarios.get(0).nome());

        assertNull(usuarios.get(1).nome());
        assertEquals(3, usuarios.get(1).numeroLinha());

        assertEquals("Pedro", usuarios.get(2).nome());
        assertEquals(4, usuarios.get(2).numeroLinha());
    }
}