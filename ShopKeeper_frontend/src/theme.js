import { theme } from 'antd'

/** 店管家设计令牌：松烟墨绿 + 朱砂印章 + 宣纸暖米 */
export const brand = {
  pine: '#215c4d',
  pineDeep: '#16392f',
  pineHover: '#2c6f5c',
  pineLight: '#e9f0ec',
  cinnabar: '#c2453b',
  paper: '#f5f2ea',
  card: '#fffef9',
  ink: '#1d2a26',
  gold: '#b08d4f',
  line: '#e6e3d9'
}

export default {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: brand.pine,
    colorInfo: brand.pine,
    colorLink: brand.pine,
    colorError: brand.cinnabar,
    colorSuccess: '#3d8b6e',
    colorTextBase: brand.ink,
    colorBgLayout: brand.paper,
    fontFamily:
      "'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
    fontSize: 14,
    borderRadius: 8,
    wireframe: false
  },
  components: {
    Layout: {
      headerBg: brand.card,
      siderBg: brand.pineDeep,
      bodyBg: brand.paper
    },
    Menu: {
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemColor: 'rgba(255,255,255,0.72)',
      darkItemHoverBg: 'rgba(255,255,255,0.08)',
      darkItemHoverColor: 'rgba(255,255,255,0.95)',
      darkItemSelectedBg: 'rgba(255,255,255,0.13)',
      darkItemSelectedColor: '#ffffff',
      itemBorderRadius: 8,
      itemMarginInline: 10,
      itemMarginBlock: 4,
      itemHeight: 44
    },
    Card: {
      colorBgContainer: brand.card,
      borderRadiusLG: 10,
      boxShadowTertiary:
        '0 1px 2px rgba(29,42,38,0.04), 0 6px 18px rgba(29,42,38,0.06)'
    },
    Table: {
      headerBg: '#edf1ed',
      headerColor: '#3c4f48',
      headerSplitColor: 'transparent',
      rowHoverBg: '#f3f7f4',
      borderColor: '#ece9df',
      cellPaddingBlock: 12
    },
    Button: {
      primaryShadow: 'none'
    },
    Tag: {
      defaultBg: '#f0ede3'
    },
    Modal: {
      titleFontSize: 17
    }
  }
}
