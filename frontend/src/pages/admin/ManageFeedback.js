import React, { useEffect, useState, useCallback } from 'react';
import API from '../../api';
import { toast } from 'react-toastify';
import { FaComments, FaReply, FaEye, FaEyeSlash } from 'react-icons/fa';

export default function ManageFeedback() {
  const [feedbacks, setFeedbacks] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [page, setPage] = useState(1);
  const [replyId, setReplyId] = useState(null);
  const [replyText, setReplyText] = useState('');

  const load = useCallback(async (p = page) => {
    const r = await API.get('/admin/feedback', { params: { page: p, per_page: 15 } });
    setFeedbacks(r.data.feedbacks);
    setPagination(r.data.pagination);
    setPage(p);
  }, [page]);

  useEffect(() => { load(1); }, []); // eslint-disable-line

  const toggleRead = async (id) => {
    try { await API.post(`/admin/feedback/${id}/toggle`); load(); } catch (e) { toast.error('Failed'); }
  };

  const sendReply = async (id) => {
    if (!replyText.trim()) return;
    try {
      await API.post(`/admin/feedback/${id}/respond`, { response: replyText });
      toast.success('Response saved');
      setReplyId(null);
      setReplyText('');
      load();
    } catch (e) { toast.error('Failed'); }
  };

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title"><FaComments /> Manage Feedback</h1>

        <div className="card">
          {feedbacks.length > 0 ? feedbacks.map(fb => (
            <div key={fb.id} style={{ borderBottom: '1px solid var(--border)', padding: '1rem 0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', flexWrap: 'wrap' }}>
                <div>
                  <strong>{fb.name}</strong> ({fb.email})
                  {fb.is_read ? <span className="badge badge-success" style={{ marginLeft: '0.5rem' }}>Read</span> : <span className="badge badge-warning" style={{ marginLeft: '0.5rem' }}>Unread</span>}
                  <p style={{ margin: '0.5rem 0', color: '#555' }}><strong>Subject:</strong> {fb.subject}</p>
                  <p style={{ margin: '0.5rem 0' }}>{fb.message}</p>
                  {fb.admin_response && (
                    <p style={{ margin: '0.5rem 0', padding: '0.5rem', background: '#f0f9ff', borderRadius: '4px' }}>
                      <strong>Admin Response:</strong> {fb.admin_response}
                    </p>
                  )}
                  <small style={{ color: '#888' }}>{new Date(fb.created_at).toLocaleString()}</small>
                </div>
                <div style={{ display: 'flex', gap: '0.25rem' }}>
                  <button className="btn btn-sm btn-outline" onClick={() => toggleRead(fb.id)}>
                    {fb.is_read ? <FaEyeSlash /> : <FaEye />}
                  </button>
                  <button className="btn btn-sm btn-primary" onClick={() => { setReplyId(replyId === fb.id ? null : fb.id); setReplyText(fb.admin_response || ''); }}>
                    <FaReply />
                  </button>
                </div>
              </div>
              {replyId === fb.id && (
                <div style={{ marginTop: '0.75rem' }}>
                  <textarea className="form-control" value={replyText} onChange={e => setReplyText(e.target.value)} placeholder="Type your response..." rows={3} />
                  <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                    <button className="btn btn-sm btn-primary" onClick={() => sendReply(fb.id)}>Send</button>
                    <button className="btn btn-sm btn-outline" onClick={() => setReplyId(null)}>Cancel</button>
                  </div>
                </div>
              )}
            </div>
          )) : <p>No feedback submissions yet.</p>}

          {pagination && pagination.pages > 1 && (
            <div className="pagination" style={{ marginTop: '1rem' }}>
              {page > 1 && <button className="btn btn-sm btn-outline" onClick={() => load(page - 1)}>Previous</button>}
              <span style={{ padding: '0.5rem' }}>Page {page} of {pagination.pages}</span>
              {page < pagination.pages && <button className="btn btn-sm btn-outline" onClick={() => load(page + 1)}>Next</button>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
