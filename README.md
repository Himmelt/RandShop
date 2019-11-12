# RandShop
RandShop

### 命令
```
/rs|shop|rshop|randshop                 打开个人随机商店
/rs|shop|rshop|randshop reload|save     重载|保存 配置、商店配置、商品配置
/agood|addgood <name> <price> <amount>  添加手持物到商品池，参数为 标志名、价格、商品池数量(直接影响抽出概率)
/sbutton|setbutton <index>              设置手持物为商店底栏对应按钮图标
/rerand [player]                        刷新随机商店
```

### 配置
```yaml
# 商店行数，必须1-5，底栏为按钮栏
shopSize: 3
# 玩家刷新随机商店价格
refreshPrice: 100
# 商店标题，${player} 会替换成对应玩家名
shopTitle: ${player}'s Rand Shop
# 底栏按钮，0 - 8
buttons:
  0: # 位置
    ==: Button # 序列化识别，不要修改
    command: say test 1  # 按钮指令，${player} 会替换成对应玩家名
    serverCmd: true # 是否由服务器执行该指令, false 为玩家执行
    icon: # 按钮图标，以下内容为完整的 itemstack 标签，不知道怎么填写请使用指令设置后再微调
      type: STONE
      amount: 1
  4:
    ==: Button
    command: rerand
    serverCmd: false
    icon:
      type: MAP
      amount: 5
```
### 商品池配置
```yaml
# 商品标识名，不可重复
testGood1:
  ==: Good # 序列化识别，不要修改
  price: 0 # 价格
  amount: 1 # 该物品在商品池的数量，会影响抽出概率，详细参考 n次独立重复事件概率模型，
            # 例子：盒子中放入不同数量不同颜色的小球，随机抽出一个某颜色球的概率 = 该颜色球数量/总数
  item: # 完整 itemstack 标签
    type: APPLE
    amount: 1
testGood2:
  ==: Good
  price: 10
  amount: 1
  item:
    type: STICK
    amount: 1
```