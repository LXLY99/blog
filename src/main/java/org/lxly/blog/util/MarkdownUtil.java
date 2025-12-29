package org.lxly.blog.util;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;  // ✅ 这里改掉
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Markdown → HTML 转换工具类
 * <p>
 * 采用 Flexmark‑All（v0.64.8），默认开启：
 * <ul>
 *   <li>GitHub‑Flavored Markdown（表格、任务列表等）</li>
 *   <li>Emoji 表情</li>
 *   <li>自动链接（URL 自动转 <code>&lt;a&gt;</code>）</li>
 * </ul>
 * 如需额外扩展，只需要在 {@link #MarkdownUtil()} 中把对应的 Extension 加入 {@code options}.
 * </p>
 */
@Component
public class MarkdownUtil {

    /** 解析器 */
    private final Parser parser;

    /** 渲染器 */
    private final HtmlRenderer renderer;

    public MarkdownUtil() {
        // ---------- 1️⃣ 配置选项 ----------
        MutableDataSet options = new MutableDataSet();

        // 开启我们需要的 Extension（使用 JDK9+ 的 List.of，JDK17 完全兼容）
        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                EmojiExtension.create(),
                AutolinkExtension.create()
        ));

        // ---------- 2️⃣ 创建 Parser 与 Renderer ----------
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * 将 Markdown 文本渲染为 HTML。
     *
     * @param markdown 原始 Markdown（可以为 {@code null}）
     * @return 渲染后的 HTML（为空字符串表示 {@code markdown==null})
     */
    public String render(String markdown) {
        if (markdown == null) {
            return "";
        }
        return renderer.render(parser.parse(markdown));
    }
}
