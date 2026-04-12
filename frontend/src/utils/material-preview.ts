import { ElMessage } from 'element-plus';
import { fetchMaterialDocxPreviewApi } from '@/api/modules/material';
import type { IdValue } from '@/types/api';

export interface MaterialPreviewSource {
  id: IdValue;
  fileExt?: string | null;
}

export async function openMaterialVersionPreview(
  source: MaterialPreviewSource | null | undefined,
  fetchBlob: () => Promise<Blob>
): Promise<void> {
  const extension = normalizeExtension(source?.fileExt);

  if (extension === 'docx') {
    if (!source?.id) {
      ElMessage.warning('缺少预览文件标识');
      return;
    }

    const previewWindow = window.open('', '_blank');
    if (!previewWindow) {
      ElMessage.warning('浏览器拦截了新标签页，请先允许弹窗');
      return;
    }

    try {
      const resp = await fetchMaterialDocxPreviewApi(source.id);
      previewWindow.document.open();
      previewWindow.document.write(resp.data.htmlContent || '<!doctype html><html><body>暂无预览内容</body></html>');
      previewWindow.document.close();
    } catch (error) {
      previewWindow.close();
      const message = error instanceof Error ? error.message : 'DOCX 在线预览失败';
      ElMessage.error(message);
    }
    return;
  }

  const blob = await fetchBlob();
  const objectUrl = URL.createObjectURL(blob);
  window.open(objectUrl, '_blank');
  setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000);
}

function normalizeExtension(fileExt?: string | null): string {
  return (fileExt || '').trim().toLowerCase();
}
