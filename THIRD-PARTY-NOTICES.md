# 第三方组件

本模组包含以下第三方代码，分发时一并附上各自的许可文本。

## JLayer 1.0.1

- 用途：mp3 解码，包名 `javazoom.jl.decoder`
- 是否嵌入：是，类文件直接打进 mod jar
- 许可：GNU Lesser General Public License 2.1（LGPL-2.1）
- 许可全文：`LICENSES/LGPL-2.1.txt`
- 上游：https://github.com/umjammer/jlayer

按 LGPL-2.1 的要求，你可以用自己编译的 JLayer 替换 jar 内 `javazoom/jl/decoder/` 下的类；
如果你想这么做，替换后重新打包即可，本模组其余部分不依赖 JLayer 的修改版本。

## 其余部分

除上述组件外，本项目代码按 MIT 许可发布，见根目录 `LICENSE`。
