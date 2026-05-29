/**
 * 异步复制文本到剪贴板
 * @param {string} text 要复制的文本
 * @returns {Promise<void>} 如果成功则 resolve, 如果失败则 reject
 */
export const copyText = async (text) => {
    if (!text) {
      // 可以选择抛出错误或返回失败的 Promise
      return Promise.reject(new Error('要复制的文本不能为空'));
    }
  
    // 检查 navigator.clipboard 是否可用 (通常需要 HTTPS 或 localhost)
    if (!navigator.clipboard) {
      // 可以选择尝试旧的 document.execCommand 作为备选方案，但它正在被弃用
      console.error('Clipboard API 不可用。请确保页面在 HTTPS 或 localhost 环境下运行。');
      return Promise.reject(new Error('浏览器不支持 Clipboard API 或页面非 HTTPS'));
    }
  
    try {
      // 尝试将文本写入剪贴板
      await navigator.clipboard.writeText(text);
      // 成功时，Promise 解析 (resolve)
      return Promise.resolve(); 
    } catch (err) {
      // 失败时，记录错误并拒绝 (reject) Promise
      console.error('无法将文本写入剪贴板:', err);
      return Promise.reject(new Error('复制文本失败，请检查浏览器权限。'));
    }
  };
  
  // 如果需要，可以导出更多剪贴板相关的工具函数
  // export const otherClipboardUtil = () => { ... }; 