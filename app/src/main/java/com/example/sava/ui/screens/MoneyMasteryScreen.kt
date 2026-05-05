package com.example.sava.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sava.R
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi

private data class ArticleCardData(
    val tag: String,
    val title: String,
    val publishDate: String,
    val description: String,
    val readTime: String,
    val imageRes: Int,
    val detailAlignment: TextAlign,
    val detailParagraphs: List<String>
)

@Composable
fun MoneyMasteryScreen() {
    val adaptiveUi = rememberAdaptiveUi()
    var searchQuery by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<ArticleCardData?>(null) }
    val latestArticles = remember {
        listOf(
            ArticleCardData(
                tag = "ARTICLE",
                title = "SIP vs Lumpsum: Which is better for your goals?",
                publishDate = "May 03, 2026",
                description = "Understand the pros, cons, and which approach fits your journey.",
                readTime = "7 min read",
                imageRes = R.drawable.first,
                detailAlignment = TextAlign.Justify,
                detailParagraphs = listOf(
                    "A Systematic Investment Plan (SIP) involves investing a fixed amount at regular intervals, allowing investors to benefit from rupee cost averaging and disciplined participation in the market. This approach reduces the impact of market volatility by spreading investments over time, making it particularly suitable for individuals with steady income and a long-term investment horizon. SIPs also help mitigate the risks associated with market timing, as investments are made consistently regardless of market conditions.",
                    "Lump sum investing, on the other hand, entails deploying a significant amount of capital at one point in time, enabling the entire investment to benefit from compounding from the outset. This approach can be advantageous when markets are at relatively attractive levels or for investors with surplus funds and a higher risk tolerance, but it carries higher exposure to market timing risk. A balanced or hybrid approach, combining SIPs for ongoing investments with selective lump sum deployment during favorable market conditions, can offer a more effective strategy by blending discipline with opportunity, thereby enhancing long-term wealth creation while managing risk."
                )
            ),
            ArticleCardData(
                tag = "GUIDE",
                title = "How to Review Your Financial Plan (The Right Way)",
                publishDate = "May 03, 2026",
                description = "A simple framework to stay on track with your long-term goals.",
                readTime = "6 min read",
                imageRes = R.drawable.second,
                detailAlignment = TextAlign.Justify,
                detailParagraphs = listOf(
                    "Review your financial plan by first realigning it with your current reality. Update all goals, short, medium, and long term, based on changes in income, lifestyle, or responsibilities, and validate whether timelines and required amounts remain accurate. Reassess your asset allocation across equity, debt, and other assets to ensure it reflects your risk profile, then rebalance if market movements have caused deviations. This step ensures your strategy remains intentional rather than drifting with market performance.",
                    "Next, evaluate your investments with a disciplined, outcome-focused approach. Measure performance against relevant benchmarks and consistency over time, not short-term fluctuations. Review all SIPs, eliminate redundancies, align each investment to a specific goal, and increase contributions in line with income growth. Simplify the portfolio where needed to maintain clarity, efficiency, and control.",
                    "Finally, reinforce the plan’s foundation by auditing risk protection and efficiency. Ensure adequate insurance coverage, maintain a sufficient emergency fund, and optimize for tax impact across investments. Track your net worth periodically to assess true financial progress, and establish a structured review cadence—light quarterly reviews and a comprehensive annual review—to keep the plan aligned, resilient, and execution-focused."
                )
            ),
            ArticleCardData(
                tag = "ARTICLE",
                title = "Principles That Drive Successful Investing",
                publishDate = "May 03, 2026",
                description = "Timeless investing principles that strengthen discipline, patience, and long-term decision-making.",
                readTime = "8 min read",
                imageRes = R.drawable.third,
                detailAlignment = TextAlign.Start,
                detailParagraphs = listOf(
                    "A large share of long-term gains is often created in a relatively small portion of time. Patience and conviction matter.",
                    "Prices move frequently; intrinsic value changes gradually. That gap creates opportunity.",
                    "Compounding needs time. There is no real substitute for staying invested.",
                    "Sometimes the best decision is to do nothing and let the strategy work.",
                    "You cannot control markets, but you can control savings, process, asset allocation, and behavior.",
                    "A past bear market often looks like a missed opportunity; a future bear market tests conviction.",
                    "Markets are shaped by human behavior, and that is why they are never perfectly efficient.",
                    "Markets often move ahead of reality or fall behind it.",
                    "Buying and selling is easy; staying invested through volatility is where wealth is built.",
                    "Avoiding equity entirely can be risky, as inflation can quietly erode long-term purchasing power.",
                    "A simple strategy you follow consistently can outperform a better strategy you abandon too soon.",
                    "Equity investing is not only about market risk; it is equally about managing behavior risk."
                )
            )
        )
    }
    val normalizedQuery = searchQuery.trim().lowercase()
    val filteredArticles = remember(latestArticles, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            latestArticles
        } else {
            latestArticles.filter { article ->
                article.tag.lowercase().contains(normalizedQuery) ||
                    article.title.lowercase().contains(normalizedQuery) ||
                    article.description.lowercase().contains(normalizedQuery)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
            .padding(
                start = 20.dp.adaptive(adaptiveUi),
                top = 38.dp.adaptive(adaptiveUi),
                end = 20.dp.adaptive(adaptiveUi),
                bottom = 18.dp.adaptive(adaptiveUi)
            )
            .widthIn(max = adaptiveUi.maxContentWidth),
        verticalArrangement = Arrangement.spacedBy(24.dp.adaptive(adaptiveUi))
    ) {
        item { HeroSection(adaptiveUi) }
        
        item {
            SearchFilterRow(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                adaptiveUi = adaptiveUi
            )
        }

        item {
            SectionTitle("Latest Articles", "See all", adaptiveUi)
        }

        if (filteredArticles.isEmpty()) {
            item {
                EmptySearchState(message = "No articles matched your search.", adaptiveUi = adaptiveUi)
            }
        } else {
            items(filteredArticles) { article ->
                LatestArticleCard(
                    article = article,
                    onClick = { selectedArticle = article },
                    adaptiveUi = adaptiveUi
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi))) }
    }

    selectedArticle?.let { article ->
        ArticleDetailDialog(
            article = article,
            onDismiss = { selectedArticle = null },
            adaptiveUi = adaptiveUi
        )
    }
}

@Composable
private fun HeroSection(adaptiveUi: AdaptiveUi) {
    val heroTitleSize = when {
        adaptiveUi.isVeryCompact -> 24.sp
        adaptiveUi.isNarrow -> 28.sp
        else -> 32.sp
    }
    val heroLineHeight = when {
        adaptiveUi.isVeryCompact -> 30.sp
        adaptiveUi.isNarrow -> 34.sp
        else -> 40.sp
    }
    val bodyStyle = MaterialTheme.typography.bodyLarge.copy(
        color = DeepCharcoal.copy(alpha = 0.7f),
        fontFamily = RefinedSerif,
        fontSize = 14.sp.adaptive(adaptiveUi),
        lineHeight = 22.sp.adaptive(adaptiveUi),
        textAlign = TextAlign.Justify
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Money Mastery",
            style = MaterialTheme.typography.displayLarge.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = heroTitleSize.adaptive(adaptiveUi),
                lineHeight = heroLineHeight.adaptive(adaptiveUi)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(3f)
            ) {
                Text(
                    text = "Your learn-and-grow hub for building financial confidence and making smarter money decisions.",
                    modifier = Modifier.fillMaxWidth(),
                    style = bodyStyle
                )
            }

            Spacer(modifier = Modifier.width(16.dp.adaptive(adaptiveUi)))

            Box(
                modifier = Modifier.weight(1.1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.top),
                    contentDescription = "Money mastery illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(22.dp.adaptive(adaptiveUi))),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun SearchFilterRow(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    adaptiveUi: AdaptiveUi
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchField(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            adaptiveUi = adaptiveUi,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            adaptiveUi = adaptiveUi,
            modifier = Modifier.widthIn(
                min = if (adaptiveUi.isVeryCompact) 78.dp else 88.dp,
                max = if (adaptiveUi.isVeryCompact) 96.dp else 108.dp
            )
        )
    }
}

@Composable
private fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    adaptiveUi: AdaptiveUi,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp.adaptive(adaptiveUi)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp.adaptive(adaptiveUi))
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = DeepCharcoal.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp.adaptive(adaptiveUi))
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search articles, topics, guides...",
                        color = DeepCharcoal.copy(alpha = 0.52f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp.adaptive(adaptiveUi),
                            lineHeight = 14.sp.adaptive(adaptiveUi)
                        )
                    )
                },
                singleLine = true,
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = DeepCharcoal,
                    focusedTextColor = DeepCharcoal,
                    unfocusedTextColor = DeepCharcoal
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp.adaptive(adaptiveUi),
                    lineHeight = 16.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun FilterChip(
    adaptiveUi: AdaptiveUi,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 13.dp.adaptive(adaptiveUi),
                vertical = 13.dp.adaptive(adaptiveUi)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp.adaptive(adaptiveUi))
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = DeepCharcoal,
                modifier = Modifier.size(16.dp.adaptive(adaptiveUi))
            )
            Text(
                text = "Filter",
                color = DeepCharcoal,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun EmptySearchState(message: String, adaptiveUi: AdaptiveUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp.adaptive(adaptiveUi)),
        color = Color.White.copy(alpha = 0.94f),
        shadowElevation = 4.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp.adaptive(adaptiveUi), vertical = 16.dp.adaptive(adaptiveUi)),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.68f),
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    action: String? = null,
    adaptiveUi: AdaptiveUi
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp.adaptive(adaptiveUi)
            )
        )
        action?.let {
            Text(
                text = it,
                color = ChampagneGold,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun LatestArticleCard(
    article: ArticleCardData,
    onClick: () -> Unit,
    adaptiveUi: AdaptiveUi
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        if (adaptiveUi.isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp.adaptive(adaptiveUi),
                        vertical = 12.dp.adaptive(adaptiveUi)
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
            ) {
                TagBadge(text = article.tag, adaptiveUi = adaptiveUi)
                Image(
                    painter = painterResource(id = article.imageRes),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp.adaptive(adaptiveUi))
                        .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi))),
                    contentScale = ContentScale.Crop
                )
                ArticleCardContent(article, adaptiveUi)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp.adaptive(adaptiveUi),
                        vertical = 12.dp.adaptive(adaptiveUi)
                    ),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.width(118.dp.adaptive(adaptiveUi)),
                    verticalArrangement = Arrangement.spacedBy(8.dp.adaptive(adaptiveUi))
                ) {
                    TagBadge(text = article.tag, adaptiveUi = adaptiveUi)
                    Image(
                        painter = painterResource(id = article.imageRes),
                        contentDescription = article.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(106.dp.adaptive(adaptiveUi))
                            .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi))),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(14.dp.adaptive(adaptiveUi)))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 10.dp.adaptive(adaptiveUi))
                ) {
                    ArticleCardContent(article, adaptiveUi)
                }
            }
        }
    }
}

@Composable
private fun ArticleCardContent(
    article: ArticleCardData,
    adaptiveUi: AdaptiveUi
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))) {
        Text(
            text = article.title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp.adaptive(adaptiveUi),
                lineHeight = 18.sp.adaptive(adaptiveUi)
            )
        )
        Text(
            text = article.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.7f),
                fontSize = 10.sp.adaptive(adaptiveUi),
                lineHeight = 15.sp.adaptive(adaptiveUi)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp.adaptive(adaptiveUi))
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = DeepCharcoal.copy(alpha = 0.55f),
                    modifier = Modifier.size(17.dp.adaptive(adaptiveUi))
                )
                Text(
                    text = article.readTime,
                    color = DeepCharcoal.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp.adaptive(adaptiveUi)
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "Save article",
                tint = DeepCharcoal.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp.adaptive(adaptiveUi))
            )
        }
    }
}

@Composable
private fun ArticleDetailDialog(
    article: ArticleCardData,
    onDismiss: () -> Unit,
    adaptiveUi: AdaptiveUi
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp.adaptive(adaptiveUi))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp.adaptive(adaptiveUi))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi)),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = article.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DeepCharcoal,
                            fontFamily = RefinedSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp.adaptive(adaptiveUi),
                            lineHeight = 23.sp.adaptive(adaptiveUi)
                        )
                    )
                    Text(
                        text = article.publishDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DeepCharcoal.copy(alpha = 0.62f),
                            fontSize = 12.sp.adaptive(adaptiveUi)
                        )
                    )
                }

                if (article.detailAlignment == TextAlign.Start) {
                    article.detailParagraphs.forEachIndexed { index, paragraph ->
                        Text(
                            text = "${index + 1}. $paragraph",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = DeepCharcoal,
                                fontFamily = RefinedSerif,
                                fontSize = 15.sp.adaptive(adaptiveUi),
                                lineHeight = 24.sp.adaptive(adaptiveUi)
                            ),
                            textAlign = TextAlign.Start
                        )
                    }
                } else {
                    article.detailParagraphs.forEach { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = DeepCharcoal,
                                fontFamily = RefinedSerif,
                                fontSize = 15.sp.adaptive(adaptiveUi),
                                lineHeight = 24.sp.adaptive(adaptiveUi)
                            ),
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagBadge(text: String, adaptiveUi: AdaptiveUi) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFF5DA), RoundedCornerShape(14.dp.adaptive(adaptiveUi)))
            .border(1.dp.adaptive(adaptiveUi), Color(0xFFFFE8B0), RoundedCornerShape(14.dp.adaptive(adaptiveUi)))
            .padding(horizontal = 12.dp.adaptive(adaptiveUi), vertical = 6.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = text,
            color = ChampagneGold,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MoneyMasteryScreenPreview() {
    SavaTheme {
        MoneyMasteryScreen()
    }
}
