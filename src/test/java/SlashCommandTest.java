import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.example.SlashCommand;
import org.example.api.ApiInteraction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlashCommandTest {

    @Mock
    private ApiInteraction apiInteractionMock;

    @InjectMocks
    private SlashCommand slashCommand;

    @Test
    void getCurrencies_ReturnsCorrectEmbed() {
        String[] currencies = {"USD", "EUR"};

        when(apiInteractionMock.getCurrency("USD")).thenReturn("1 USD :flag_us: :hamburger: = 39.50 UAH");
        when(apiInteractionMock.getCurrency("EUR")).thenReturn("1 EUR :flag_eu: :rainbow_flag: = 42.10 UAH");

        EmbedBuilder builder = slashCommand.getCurrencies(currencies);
        MessageEmbed embed = builder.build();

        assertEquals("бігом в абмєнік", embed.getTitle());
        assertEquals(Color.YELLOW, embed.getColor());

        String expectedDescription = "1 USD :flag_us: :hamburger: = 39.50 UAH\n1 EUR :flag_eu: :rainbow_flag: = 42.10 UAH";
        assertEquals(expectedDescription, embed.getDescription());

        verify(apiInteractionMock, times(1)).getCurrency("USD");
        verify(apiInteractionMock, times(1)).getCurrency("EUR");
    }

    @Test
    void getCat_SiamBreed_ReturnsSiamEmbed() {
        String fakeUrl = "https://cdn2.thecatapi.com/images/siam_cat.jpg";
        when(apiInteractionMock.getCatImage("siam")).thenReturn(fakeUrl);

        EmbedBuilder builder = slashCommand.getCat("siam");
        MessageEmbed embed = builder.build();

        assertNotNull(embed);
        assertEquals("чєрнаморд", embed.getTitle());
        assertEquals(Color.ORANGE, embed.getColor());
        assertEquals(fakeUrl, embed.getImage().getUrl());
    }

    @Test
    void getCat_DefaultBreed_ReturnsDefaultEmbed() {
        String fakeUrl = "https://cdn2.thecatapi.com/images/random_cat.jpg";
        when(apiInteractionMock.getCatImage("")).thenReturn(fakeUrl);

        EmbedBuilder builder = slashCommand.getCat("");
        MessageEmbed embed = builder.build();

        assertNotNull(embed);
        assertEquals("утіпуті", embed.getTitle());
    }

    @Test
    void getCat_ApiReturnsNull_ReturnsNull() {
        when(apiInteractionMock.getCatImage("error_breed")).thenReturn(null);

        EmbedBuilder builder = slashCommand.getCat("error_breed");

        assertNull(builder, "Method must return null");
    }
}
